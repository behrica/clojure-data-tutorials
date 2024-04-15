(ns ggplotly-cont
  (:require [tablecloth.api :as tc]
            [scicloj.metamorph.ml.toydata.ggplot :as toydata.ggplot]
            [tech.v3.datatype.functional :as fun]
            [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str]
            [clojure.math :as math]
            [clojure2d.color :as color]))

;; # Further exploring ggplotly's client side

;; The following template was generated from the JSON file
;; in the HTML generated by
;; `ggplotly(ggplot(mpg, aes(x=hwy, y=displ, color=factor(cyl))) + geom_point() + geom_smooth(method="lm"))`
;; and then gradually generalizing it as a set of Clojure functions.
;; This is a work-in-progress draft. Some parts are still hard-coded (e.g., colours).


(defn layer [{:keys [type x y color text name legendgroup showlegend]}]
  (merge {:hoveron "points",
          :legendgroup legendgroup,
          :showlegend showlegend
          :frame nil,
          :hoverinfo "text",
          :name name,
          :mode (case type
                  :point "markers"
                  :line "lines")
          :type "scatter",

          :xaxis "x",
          :yaxis "y",
          :x (vec x)
          :y (vec y)
          :text (vec text)}
         (case type
           :point {:marker {:autocolorscale false,
                            :color color,
                            :opacity 1,
                            :size 5.66929133858268,
                            :symbol "circle",
                            :line {:width 1.88976377952756, :color color}}}
           :line {:line {:width 3.77952755905512,
                         :color color,
                         :dash "solid"}})))

(color/palette )

(def colors
  ["rgba(248,118,109,1)"
   "rgba(124,174,0,1)"
   "rgba(0,191,196,1)"
   "rgba(199,124,255,1)"])

(def config
  {:doubleClick "reset",
   :modeBarButtonsToAdd ["hoverclosest" "hovercompare"],
   :showSendToCloud false},)

(def highlight
  {:on "plotly_click",
   :persistent false,
   :dynamic false,
   :selectize false,
   :opacityDim 0.2,
   :selected {:opacity 1},
   :debounce 0},)

(defn texts [ds column-names]
  (-> ds
      (tc/select-columns column-names)
      tc/rows
      (->> (map (fn [row]
                  (str/join "<br />"
                            (map (partial format "%s: %s")
                                 column-names row)))))))

(defn ->tickvals [l r]
  (let [jump (-> (- r l)
                 (/ 6)
                 math/floor
                 int
                 (max 1))]
    (-> l
        math/ceil
        (range r jump))))

(defn axis [{:keys [minval maxval anchor title]}]
  (let [tickvals (->tickvals minval maxval)
        ticktext (mapv str tickvals)
        range-len (- maxval minval)
        range-expansion (* 0.1 range-len)
        expanded-range [(- minval range-expansion)
                        (+ maxval range-expansion)]]
    {:linewidth 0,
     :ticklen 3.65296803652968,
     :tickcolor "rgba(51,51,51,1)",
     :tickmode "array",
     :gridcolor "rgba(255,255,255,1)",
     :automargin true,
     :type "linear",
     :tickvals tickvals,
     :zeroline false,
     :title
     {:text title,
      :font {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187}},
     :tickfont {:color "rgba(77,77,77,1)", :family "", :size 11.689497716895},
     :autorange false,
     :showticklabels true,
     :showline false,
     :showgrid true,
     :ticktext ticktext,
     :ticks "outside",
     :gridwidth 0.66417600664176,
     :anchor "y",
     :domain [0 1],
     :hoverformat ".2f",
     :tickangle 0,
     :tickwidth 0.66417600664176,
     :categoryarray ticktext,
     :categoryorder "array",
     :range expanded-range},))
,

(defn layout [{:keys [xaxis yaxis]}]
  {:plot_bgcolor "rgba(235,235,235,1)",
   :paper_bgcolor "rgba(255,255,255,1)",
   :legend {:bgcolor "rgba(255,255,255,1)",
            :bordercolor "transparent",
            :borderwidth 1.88976377952756,
            :font {:color "rgba(0,0,0,1)", :family "", :size 11.689497716895},
            :title {:text "factor(cyl)",
                    :font {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187}}},
   :font {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187},
   :showlegend true,
   :barmode "relative",
   :hovermode "closest",
   :margin
   {:t 25.7412480974125,
    :r 7.30593607305936,
    :b 39.6955859969559,
    :l 31.4155251141553},
   :shapes [{:yref "paper",
             :fillcolor nil,
             :xref "paper",
             :y1 1,
             :type "rect",
             :line {:color nil, :width 0, :linetype []},
             :y0 0,
             :x1 1,
             :x0 0}]
   :xaxis xaxis
   :yaxis yaxis})


(delay
  (let [data toydata.ggplot/mpg
        point-layers (-> data
                         (tc/add-column "factor(cyl)" (:cyl %))
                         (tc/group-by :cyl {:result-type :as-map})
                         (->> (sort-by key)
                              (map-indexed
                               (fn [i [group-name group-data]]
                                 (let [base {:x (:hwy group-data),
                                             :y (:displ group-data)
                                             :color (colors i)
                                             :name group-name
                                             :legendgroup group-name}
                                       predictions (map
                                                    (fun/linear-regressor (:hwy group-data)
                                                                          (:displ group-data))
                                                    (:hwy group-data))]
                                   [(-> base
                                        (assoc :type :point
                                               :showlegend true
                                               :y (:displ group-data)
                                               :text (-> group-data
                                                         (texts [:hwy :displ "factor(cyl)"])))
                                        layer)
                                    (-> base
                                        (assoc :type :line
                                               :showlegend false
                                               :y predictions
                                               :text (-> group-data
                                                         ;; (tc/add-column :displ predictions)
                                                         (texts [:hwy :displ "factor(cyl)"])))
                                        layer)])))
                              (apply concat)))
        xmin (-> data :hwy fun/reduce-min)
        xmax (-> data :hwy fun/reduce-max)
        ymin (-> data :displ fun/reduce-min)
        ymax (-> data :displ fun/reduce-max)
        xaxis (axis {:minval xmin
                     :maxval xmax
                     :anchor "x"
                     :title :hwy})
        yaxis (axis {:minval ymin
                     :maxval ymax
                     :anchor "x"
                     :title :displ})]
    (kind/htmlwidgets-ggplotly
     {:x
      {:config config
       :highlight highlight
       :base_url "https://plot.ly",
       :layout (layout {:xaxis xaxis
                        :yaxis yaxis})
       :data point-layers},
      :evals [],
      :jsHooks []})))
