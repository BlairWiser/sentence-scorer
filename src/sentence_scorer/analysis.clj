(ns sentence-scorer.analysis
  (:use [sentence-scorer.evaluate])
  (:require [clojure.string :as str]))

(defn get-weighted-mean
  "Takes a list and returns the mean"
  [scores]
  (let [k (count scores)
        W (repeat k 1)
        terms (for [i (range k) :let [Wi (nth W i)]]
                (if (Double/isNaN (nth scores i))
                  (* Wi -100)
                  (* Wi (nth scores i))))]
    (/ (apply + terms) k)))

(defn get-n-grams
  "Takes a sentence and returns its n-gram scores for n = 5 through 1"
  [lm sentence]
  (let [words (str/split sentence #"\s+")]
    (for [i (range 5 0 -1)]
      (:spanScores (score-by-blm {:words words :N i :lm lm})))))

(defn get-sentence-vectors
  "Takes n-gram scores and returns aggregated relative scores"
  [ngrams]
  (for [i (range 0 5) :let [s (nth ngrams i)]]
    (get-weighted-mean s)))

(defn aggregate-vectors
  "Takes a list of score vectors and returns average"
  [scores]
  (let [k (count scores)]
    (doall (for [i (range 5)]
      (/ (apply + (map #(nth % i) scores)) k)))))
