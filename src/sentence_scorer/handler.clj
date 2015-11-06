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

(defn score-text
  "Takes a block of text and returns the scores for the sentences"
  [text]
  ;;Split on commas and periods followed by whitespace
  (let [sentences (str/split text #",\s|\.\s+|!\s|\?\s")]
    (vector sentences (map score-sentence sentences))))

(defroutes app-routes
  (GET "/:collectionID/:fileID/" [collectionID fileID]
       (let [start     (System/currentTimeMillis)
             filename  (str base-dir "/" collectionID "/" fileID)
             text      (slurp filename)
             result    (score-text text)
             scores    (get result 1)
             score-vec (aggregate-vectors scores)]
         {:status 200
          :body {:collection collectionID
                 :file       fileID
                 :score      score-vec
                 :time       (- (System/currentTimeMillis) start)}}))

  (GET "/:collectionID/" [collectionID]
       (let [start        (System/currentTimeMillis)
             dirname      (str base-dir "/" collectionID)
             filenames    (.list (io/file dirname))
             results      (doall 
                            (for [x filenames]
                              (let [text   (slurp (str dirname "/" x))
                                    scores (get (score-text text) 1)]
                                {:name x
                                 :score (aggregate-vectors scores)})))]

         {:status   200
          :body     {:collection collectionID
                     :articles   (sort-by :name results)
                     :time       (- (System/currentTimeMillis) start)}}))


  (route/not-found "Not Found"))

(def app
(-> (handler/site app-routes)
  (middleware/wrap-json-body {:keywords? true}) 
  (middleware/wrap-json-response)))
