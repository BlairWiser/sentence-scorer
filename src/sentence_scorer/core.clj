(ns sentence-scorer.core
  (:use [sentence-scorer.evaluate]
        [sentence-scorer.analysis])
  (:require [clojure.string :as str]))

(def lm (make-google-lm))
(def directory "resources/public/")

(defn score-sentence
  "Takes a sentence and returns score vector"
  [sentence]
  (get-sentence-vectors (get-n-grams lm sentence)))

(defn get-file-lines
  "Takes a filename and returns the file's sentences"
  [filename]
  (let [content (slurp filename)]
    ;;Split on commas and periods followed by whitespace
    (str/split content #",\s|\.\s+"))) 

(defn score-file
  "Takes a filename and returns the scores for the sentences"
  [filename]
  (let [file-path (str directory filename)
        lines (get-file-lines file-path)]
    (zipmap lines (map score-sentence lines))))

(defn user-loop
  []
  (println "Enter 1 to evaluate sentences, 2 to evaluate a file, 0 to quit:")
  (loop [userChoice (read-line)]
    (case userChoice
      "0" (println "Exiting. Goodbye!")
      "1" (do 
            (println "Enter two sentences to compare:")
            (let [s1 (read-line)
                 score1 (vec (score-sentence s1))
                 s2 (read-line)
                  score2 (vec (score-sentence s2))]
              (println (str "Sentence 1: " score1))
              (println (str "Sentence 2: " score2))
              (println (str "Comparator: " (compare score1 score2))))
            (println "Enter 1 to evaluate sentences, 2 to evaluate a file, 0 to quit:")
            (recur (read-line)))
      "2" (do
            (println "Enter filename")
            (let [filename (read-line)
                  score-map (score-file filename)]
              (println (str score-map)))
            (println "Enter 1 to evaluate sentences, 2 to evaluate a file, 0 to quit:")
            (recur (read-line))))))

(defn -main
  [& args]
  (if (= (count args) 0)
    (user-loop)
    (let [score-maps (map score-file args)]
      (doseq [i score-maps]
        (println (str i "\n"))))))
    
    