(ns starter.badges
  (:require [helix.core :refer [$]]
            [helix.dom :as d]
            [kushi.core :refer [merge-attrs sx]]
            [kushi.ui.core :refer [opts+children]]
            [kushi.ui.icon.mui.core :refer [kw->string]]
            [kushi.ui.tooltip.events :refer (tooltip-mouse-leave tooltip-mouse-enter)]))

;; DEFINING COMPONENTS
;; Also see https://github.com/kushidesign/kushi#defining-components
;; ...............................................................

;; The commented code below is a contrived example of creating a reusable, stateless, and composable component using `kushi.ui.core/defcom`.

;; (ns myapp.core
;;   (:require
;;    [kushi.core :refer [sx]]
;;    [kushi.ui.core :refer [defcom]]))

;; (defcom my-section
;;   (let [{:keys [label label-attrs body-attrs]} &opts]
;;     [:section
;;      &attrs
;;      (when label
;;        [:div label-attrs label])
;;      [:div body-attrs &children]]))

;; `defcom` is a macro that returns a component rendering function which accepts an optional attributes map, plus any number of children.
;; This means the signature at the call site mirrros hiccup itself.

;; Under the hood, defcom pulls out any keys in attr map that start with `:-` and put them in a separate `opts` map.
;; This allows passing in various custom options within the attributes map that will not clash with existing html attributes.
;; This opts map can referenced be referenced in the defcom body with the `&opts` binding. `&attrs` and `&children` are also available.
;; This ampersand-leading naming convention takes its cue from the special `&form` and `&env` bindings used by Clojure's own `defmacro`.

;; Assuming you are using something like Reagent, you can use the resulting `my-section` component in your application code like so:

;; Basic, no label
;; [my-section [:p "Child one"] [:p "Child two"]]

;; With optional label
;; [my-section (sx {:-label "My Label"}) [:p "Child one"] [:p "Child two"]]

;; With all the options and additional styling
;; [my-section
;;  (sx
;;   'my-section-wrapper    ; Provides custom classname (instead of auto-generated).
;;   :.xsmall               ; Font-size utility class.
;;   :p--1rem               ; Padding inside component.
;;   :b--1px:solid-black    ; Border around component.
;;   {:-label "My Label"
;;    :-label-attrs (sx :.huge :c--red)
;;    :-body-attrs (sx :bgc--#efefef)})
;;  [:p "Child one"]

;; For more in-depth info on defmacro and its underlying pattern,
;; see https://github.com/kushidesign/kushi#manually-defining-complex-components.

;; Below, `contained-image` and `twirling-badge` both use defmacro to create reusable components.
;; They also use kushi.core/merge-attrs to merge user-passed attributes.
(defn tooltip
  {:desc ["Tooltips provide additional context when hovering or clicking on an element."
          :br
          :br
          "Tooltips in Kushi have no arrow indicator and are placed automatically depending on the parent element's relative postition in the viewport."
          "Tooltips are placed above or below the parent element that they describe."
          :br
          :br
          "The font-size for the tooltip should be set with the `:$tooltip-font-size` token."
          :br
          :br
          "Overriding the automatic inline or block (or both) placement is available via the options `:-block-offset` and `:-inline-offset`."
          :br
          :br
          "Forcing centered-aligned placement to the top, bottom, left, or right of the parent item can be done with the `:-placement` option."
          :br
          :br
          "Exact placement can be achieved via css."]
   :opts '[{:name    display-on-hover?
            :type    :boolean
            :default true
            :desc    "Setting to `false` will conditionalize display based on user event such as a click"}
           {:name    block-offset
            :type    #{:start :end}
            :default nil
            :desc    "Setting to either `:start` or `:end` will override vertical auto-placement"}
           {:name    inline-offset
            :type    #{:start :end}
            :default nil
            :desc    "Setting to either `:start` or `:end` will override horizontal auto-placement"}
           {:name    placement
            :type    #{:inline-start :inline-end :block-start :block-end}
            :default nil
            :desc    "Setting to one of the accepted values will override horizontal and vertical auto-placement. When used, any supplied values for `:-inline-offset` and `:-block-offset` will be ignored."}]}
  [& args]
  (let [[opts attr & children]      (opts+children args)
        {:keys [display-on-hover? inline-offset block-offset placement]} opts]
    (d/section
     {:& (let [id (gensym)
               placement-class (when placement (str "kushi-tooltip-placement-" (name placement)))]
           (merge-attrs
            (sx 'kushi-tooltip
                :.absolute
                :.rounded
                :fs--$tooltip-font-size
                :bgc--$tooltip-background-color
                :c--$tooltip-color
                :top--0
                :bottom--unset
                :left--100%
                :right--unset
                :p--0
                :m--5px
                :ws--n
                :o--0
                :w--0
                :h--0
                :overflow--hidden
                :transition--opacity:0.2s:linear
             ;; maybe abstract into an :.overlay defclass(es) with decoration defclasses for tooltip vs popover
                :line-height--1.5
                ["has-ancestor([data-kushi-tooltip='true'][aria-expanded='true']):transition"     :none]
                ["has-ancestor([data-kushi-tooltip='true'][aria-expanded='true']):opacity"        1]
                ["has-ancestor([data-kushi-tooltip='true'][aria-expanded='true']):width"          :fit-content]
                ["has-ancestor([data-kushi-tooltip='true'][aria-expanded='true']):height"         :auto]
                ["has-ancestor([data-kushi-tooltip='true'][aria-expanded='true']):padding-block"  :$tooltip-padding-block]
                ["has-ancestor([data-kushi-tooltip='true'][aria-expanded='true']):padding-inline" :$tooltip-padding-inline]
                {:class                              [placement-class]
                 :data-kushi-conditional-display     (if (= false display-on-hover?) "true" "false")
                 :data-kushi-tooltip-position-block  block-offset
                 :data-kushi-tooltip-position-inline inline-offset
                 :id                                 id
                 :key                                id
                 :data-kushi-ui                      :tooltip})
            #_(when (= placement :inline-end)
                {:style {:left      "calc(100% + 12px)!important"
                         :right     :unset!important
                         :top       :50%!important
                         :transform "translateY(-50%)!important"
                         :margin    :0!important}})
            attr))}
     children)))

(defn mui-icon
  [attrs children]
  (d/div
   {:& (merge (sx
               'kushi-mui-icon
               {:data-kushi-ui :mui-icon})
              attrs)}
   ($ "span" {:class "material-icons"} (kw->string children))))

(defn contained-image
  [{:keys [src]}]
  (d/img {:src src
          :& (sx 'grayscale-icon-image
                 :.grayscale
                 :.small-badge
                 :max-height--100%
                 :max-width--100%
                 :object-fit--contain)}))

(defn icon-badge-link
  [{:keys [id href target]} children]
  (d/a {:key id
        :href href
        :target target
        :& (sx :hover:bgc--transparent
               :hover:c--white
               :bgc--transparent
               :p--0)}
       (d/button {:& (sx 'kushi-button
                         :.flex-row-c
                         :.transition
                         :.pointer
                         :.relative
                         {:data-kushi-ui      :button
                          :data-kushi-tooltip true
                          :aria-expanded      "false"
                          :on-mouse-enter     tooltip-mouse-enter
                          :on-mouse-leave     tooltip-mouse-leave})}
                 children)))

(def link-data
  [{:id :github
    :href "https://github.com/kushidesign/kushi"
    :src  "graphics/github.svg"
    :inline-offset  "end"
    :tooltip-text (d/span {:key :github} "View project on github " (mui-icon nil "open_in_new"))}
   {:id :clojars
    :href "https://clojars.org/design.kushi/kushi"
    :src  "graphics/clojars-logo-bw2.png"
    :inline-offset "start"
    :tooltip-text (d/span {:key :clojar} "View project at clojars.org " (mui-icon nil "open_in_new"))}
   #_{:href "https://twitter.com"
      :src  "graphics/twitter.svg"}])

;; Usage of the components defined above
;; Also uses the kushi.ui.tooltip.core/tooltip component from kushi's set of primitive ui components.
(defn links []
  (d/div
   {:& (sx :.absolute
           :.flex-row-c
           :w--100%
           :mbs--38px)}
   (d/div
    {:& (sx :.flex-row-sa
            :w--100px
            :>button:display--inline-flex)}

    (for [{:keys [id href src inline-offset tooltip-text]} link-data]
      (icon-badge-link {:id id :href href :target "_blank"}
                       (d/span {:& (sx 'kushi-label
                                       :.flex-row-c
                                       :gap--$icon-enhancer-inline-gap-ems
                                       :.transition
                                       :ai--c
                                       :d--inline-flex
                                       :w--fit-content
                                       {:data-kushi-ui :label}
                                       :flex-direction--column-reverse)}
                               (contained-image {:src src})
                               (tooltip (sx :ff--FiraCodeRegular|monospace|sans-serif
                                            {:-inline-offset inline-offset})
                                        tooltip-text)))))))
