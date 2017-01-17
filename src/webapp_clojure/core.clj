(ns webapp-clojure.core
  (:require [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [hiccup.core :as h]
            [ring.middleware.params :as p]
            [ring.util.response :as r])
  (:gen-class))

(defonce server (atom nil))
(defonce whiskeys (atom []))

(c/defroutes app
  (c/GET "/" []
    (h/html [ :html
             [:body
              [:form {:action "/add-whiskey" :method "post"}
               [:input {:type "text" :placeholder "Enter your whiskey" :name "whiskey"}]
               [:input {:type "text" :placeholder "Enter year" :name "year"}]
               [:input {:type "text" :placeholder "Enter purchase price" :name "price"}]
               [:button {:type "submit"} "Add Your Bottle"]]
              [:ol
               (map (fn [whiskey]
                      [:li whiskey])
                 @whiskeys)]]]))
  
  (c/POST "/add-whiskey" request
    (let [params (get request :params)
          name (get params "whiskey")
          year (get params "year")
          price(get params "price")
          
          whiskey(conj[year ",  " name ",  " price])]
      
      (swap! whiskeys conj whiskey)
      
      (spit "whiskey.edn" (pr-str @whiskeys))
      (r/redirect "/"))))

(defn -main []
  (try
    (let [whiskeys-str (slurp "whiskeys.edn")
           whiskeys-vec (read-string whiskeys-str)]
      (reset! whiskeys whiskeys-vec))
    (catch Exception _))
  (when @server
    (.stop @server))
  (let [app (p/wrap-params app)]
    (reset! server (j/run-jetty app {:port 3000 :join? false}))))
