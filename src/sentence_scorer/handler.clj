(ns sentence-scorer.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp]
            [clojure.string :as str])
  (:use [sentence-scorer.evaluate]
        [sentence-scorer.analysis]))

(def root (str (System/getProperty "user.dir") "/resources/public"))
(def lm (make-google-lm))

(defn param
  "Extract parameter from a request"
  [request name]
  (get-in request [:params name]))

(defn score-sentence
  "Takes a sentence and returns score vector"
  [sentence]
  (get-sentence-vectors (get-n-grams lm sentence)))

(defn score-file
  "Takes a block of text and returns the scores for the sentences"
  [text]
  ;;Split on commas and periods followed by whitespace
  (let [lines (str/split text #",\s|\.\s+|!\s|\?\s")]
    (vector lines (map score-sentence lines))))

(defroutes app-routes
  ;;(route/files "/" {:root root})
  (POST "/score/" request
       (let [text (param request :text)
             result (score-file text)
             lines (first result)
             scores (get result 1)]
         {:status 200
          :body {:overall (aggregate-vectors scores)
                 :sentenceScores (zipmap (reverse lines) (reverse scores))}}))
  (route/not-found "Not Found"))

(def app
(-> (handler/site app-routes)
  (middleware/wrap-json-body {:keywords? true}) 
  (middleware/wrap-json-response)))
