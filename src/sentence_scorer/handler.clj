(ns sentence-scorer.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:use [sentence-scorer.evaluate]
        [sentence-scorer.analysis]))

(def lm (make-google-lm))
(def base-dir (System/getenv "COLLECTIONS_UPLOAD_FOLDER"))

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
  (GET "/:collectionID/:fileID/" [collectionID fileID]
       (let [timeStart (System/currentTimeMillis)
             filename (str base-dir "/" collectionID "/" fileID)
             text (slurp filename)
             result (score-file text)
             score (get result 1)
             resultMap {:status 200
                        :body {:name collectionID
                        :articles {:name fileID
                                   :score (aggregate-vectors score)}}}]
            (assoc resultMap :body (assoc (:body resultMap) :time (/ (- (System/currentTimeMillis) timeStart) 1000)))))

  (GET "/:collectionID/" [collectionID]
       (let [timeStart (System/currentTimeMillis)
             dirname (str base-dir "/" collectionID)
             filenames (.list (io/file dirname))
             results (for [x filenames :let [text (slurp (str dirname "/" x))
                                             score (get (score-file text) 1)]] 
                       (hash-map :name x, :score (aggregate-vectors score)))
             sortResults (sort-by :name results)
             resultMap {:status 200
                        :body {:name collectionID
                        :articles sortResults
                        }}]
          (assoc resultMap :body (assoc (:body resultMap) :time (/ (- (System/currentTimeMillis) timeStart) 1000)))))
  
  (route/not-found "Not Found"))

(def app
(-> (handler/site app-routes)
  (middleware/wrap-json-body {:keywords? true}) 
  (middleware/wrap-json-response)))
