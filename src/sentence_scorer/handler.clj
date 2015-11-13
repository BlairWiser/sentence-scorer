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

(defn split-into-sentences [text]
  (str/split text #",\s|\.\s+|!\s|\?\s"))

(defn score-sentence
  "Takes a sentence and returns score vector"
  [sentence]
  (get-sentence-vectors (get-n-grams lm sentence)))

(defn score-text
  "Takes a block of text and returns the scores for the sentences"
  [text]
  (let [sentences (split-into-sentences text)]
    (vector sentences (map score-sentence sentences))))


(defn score-detail-for-sentence
  "Obtain n-gram details of the given sentence"
  [sentence]
  (let [words (get-words sentence)]
    (loop [i      1
           result {}]
      (if (<= i 5)
        (let [scores (:spanScores (score-by-blm {:words words :N i :lm lm}))
              ngrams (make-ngrams i words)
              ngram-scores (into {} (map vector ngrams scores))]
          (recur (inc i) (merge result ngram-scores)))
        result))))

(defn score-detail 
  "Returns the list of top *worst* ngrams"
  [text top]
  (let [sentences  (split-into-sentences text)
        score-maps (map score-detail-for-sentence sentences)
        score-pairs (into [] (apply merge score-maps))
        get-score   #(get % 1)]
    (take top (sort-by get-score (filter #(> (get-score %) -200) score-pairs)))))

(defroutes app-routes
  (GET "/nlp/:collectionID/:fileID/" [collectionID fileID detailed top]
       (let [start     (System/currentTimeMillis)
             filename  (str base-dir "/" collectionID "/" fileID)
             text      (slurp filename)
             result    (score-text text)
             scores    (get result 1)
             k         (try (Integer/parseInt top)
                            (catch Exception e 100))
             detail    (if (nil? detailed) 
                         nil 
                         {:text text,
                          :scores (score-detail text k)})
             score-vec (aggregate-vectors scores)]
         {:status 200
          :body {:collection collectionID
                 :file       fileID
                 :score      score-vec
                 :detail     detail
                 :time       (- (System/currentTimeMillis) start)}}))

  (GET "/nlp/:collectionID/" [collectionID]
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

  (route/files "/" {:root "presentation"})
  (route/files "/article/" {:root "app"})
  (route/not-found "Not Found"))

(def app
(-> (handler/site app-routes)
  (middleware/wrap-json-body {:keywords? true}) 
  (middleware/wrap-json-response)))
