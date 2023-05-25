(ns scrapbook.visualization
  (:require [tablecloth.api :as tc]
            [aerial.hanami.templates :as ht]
            [scicloj.noj.v1.vis :as vis]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as fun]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kind :as kind]
            [hiccup.core :as hiccup]
            hiccup.util))

;; # Data visualization

;; ## Visualizing datases with Hanami
(let [n 19]
  (-> {:x (range n)
       :y (map +
               (range n)
               (repeatedly n rand))}
      tc/dataset
      (vis/hanami-plot ht/point-chart
                       :MSIZE 200)))


:bye
