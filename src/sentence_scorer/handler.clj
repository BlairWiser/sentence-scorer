(ns sentence-scorer.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp])
  (:use [sentence-scorer.evaluate]
        [sentence-scorer.analysis]))

(def root (str (System/getProperty "user.dir") "/resources/public"))
(def lm (make-google-lm))

(defn param
  "Extract parameter from a request"
  [request name]
  (get-in request [:params name]))

(defn score-sentence
  "Returns n-gram scores of sentence for n 1 through 5"
  [sentence]  
  (let [words (tokenize sentence)
        blm1 (score-by-blm {:words words :N 1 :lm lm})
        blm2 (score-by-blm {:words words :N 2 :lm lm})
        blm3 (score-by-blm {:words words :N 3 :lm lm})
        blm4 (score-by-blm {:words words :N 4 :lm lm})
        blm5 (score-by-blm {:words words :N 5 :lm lm})]
    {:status 200
     :body {:words words
            :blm_1 blm1
            :blm_2 blm2
            :blm_3 blm3
            :blm_4 blm4
            :blm_5 blm5}}))

(defroutes app-routes
  ;;(route/files "/" {:root root})
  (POST "/score/" request
        (let [sentence (param request :sentence)]
          (score-sentence sentence)))
  (route/not-found "Not Found"))

(def app
(-> (handler/site app-routes)
  (middleware/wrap-json-body {:keywords? true}) 
  (middleware/wrap-json-response)))
