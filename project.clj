(defproject bots "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src" #_"robbit"]
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.novemberain/monger "1.2.0"]
                 [enlive              "1.0.1"]
                 [robbit "1.0.0-SNAPSHOT"]
                 ; Robbit deps
                 #_[clj-http            "0.5.2"]
                 #_[cheshire            "4.0.1"]
                 #_[clj-time            "0.4.3"]])
