(ns sentence-scorer.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp]
            [clojure.string :as str])
  (:use [sentence-scorer.evaluate]
        [sentence-scorer.analysis]))

(def lm (make-google-lm))
(def base-dir (System/getenv "COLLECTIONS_UPLOAD_FOLDER"))

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
  (GET "/:collectionID/:fileID" [collectionID fileID]
       (let [filename (str base-dir "/" collectionID "/" fileID)
             text (slurp filename)
             result (score-file text)
             score (get result 1)]
         {:status 200
          :body {:name collectionID
                 :articles {:name fileID
                            :score (aggregate-vectors score)}}}))
  (route/not-found "Not Found"))

(def app
(-> (handler/site app-routes)
  (middleware/wrap-json-body {:keywords? true}) 
  (middleware/wrap-json-response)))
