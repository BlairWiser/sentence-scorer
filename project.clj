(defproject demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :jvm-opts ["-Xmx10g" "-server"]
  :repositories {"local" ~(str (.toURI (java.io.File. "local_mvn_repo")))}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clojure-opennlp "0.3.3"]
                 [berkeleylm/berkeleylm "1.1.5"]
                 [io.aviso/pretty "0.1.17"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.3.1"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler sentence-scorer.handler/app}
  :main sentence-scorer.core
  :repl-options { :nrepl-middleware [io.aviso.nrepl/pretty-middleware] })
