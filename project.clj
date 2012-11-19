(defproject bots "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src" "robbit"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.novemberain/monger "1.2.0"]
                 [enlive              "1.0.1"]
                 ; Robbit deps
                 [clj-http            "0.5.2"]
                 [cheshire            "4.0.1"]
                 [clj-time            "0.4.3"]])
