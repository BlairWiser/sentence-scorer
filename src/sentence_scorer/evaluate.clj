(ns sentence-scorer.evaluate
	(:use opennlp.nlp)
	(:import (edu.berkeley.nlp.lm.io LmReaders)))

(def tokenize (make-tokenizer "resources/models/en-token.bin"))
(def get-sentences (make-sentence-detector "resources/models/en-sent.bin"))

(defn make-google-lm
  "Initialize google language model"
  []
  (do
    (println "Loading the Google language model.")
    (let [modeldir (System/getenv "LMDIR") ;; environtment variable with directory of lm files
          vocab (str modeldir "/vocab_cs.gz")
          lm (str modeldir "/eng.blm")
          goog-lm (if (empty? modeldir)
                    (throw (Exception. "Environment variable LMDIR is not set"))
                    (LmReaders/readGoogleLmBinary lm vocab))]
      (println "Completed")
      goog-lm)))

;; Fake lm for debug purposes
(defn make-google-lm-fake [] nil)

(defn get-log-prob 
  "Calls the java function getLogProb on given n-gram"
  [n-gram lm]
  (if (nil? lm)
    -100
    (.getLogProb lm n-gram)))

(defn make-ngrams
  "Returns a seq for each consecutive n words in words"
  [n words]
  (let [L (max 0 (- (count words) n))]
    (for [i (range 0 (inc L))]
     (subvec words i (min (count words) (+ i n))))))

(defn score-by-blm
  "words:sentence<string>, N:n-gram length<int>"
  [{:keys [words N lm]}]
  (let [n-grams (make-ngrams N words)
        probs (map #(get-log-prob % lm) n-grams)
        total-prob (apply + probs)]
    {:sentenceScore total-prob
     :spanScores probs
     :spanSize N}))
