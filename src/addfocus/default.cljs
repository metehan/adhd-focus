(ns addfocus.default
  (:require
    [re-frame.core :as rf]
    [reagent.core :as reagent]))


;; 1. Event Dispatch
;;    make a view, dispatch an event in a click handler

(defn counter
  "Given a counter id and its current value, render it as an interactive widget."
  [id]
  (let [counter-value @(rf/subscribe [::counter id])]
    [:div {:on-click #(rf/dispatch [:inc-counter id])
           :style    {:padding    20
                      :margin     "10px 0"
                      :background "rgba(0,0,0,0.05)"
                      :cursor     "pointer"}}
     (str "Counter " (name id) ": ")
     counter-value]))


;; 2. Event Handling
;;    register a handler for a given event.
;;
;;    - handlers are identified by keyword.
;;    - simple method:   `reg-event-db` is passed `db` as 1st argument.
;;      advanced method: `reg-event-fx` is passed 'co-effects' map as 1st argument of which `:db` is one key.

(rf/reg-event-db :inc-counter
                 (fn [db [_ counter-id]]
                   (update-in db [::counters counter-id] inc)))


(rf/reg-event-db :initialize
                 ;; we'll call this once, at the beginning, to set up the db.
                 (constantly {::counters {"A" 0
                                          "B" 0
                                          "C" 0}}))

;; 3. Queries
;;    make a query for every kind of 'read' into the db.
;;
;;    - queries are identified by keyword.
;;    - queries can (optionally) take parameters.
;;    - `db` is passed as 1st arg to function.
;;      vector of [query-id & args] is passed as 2nd arg.

(rf/reg-sub ::counter
            (fn [db [_ counter-id]]
              (get-in db [::counters counter-id])))

(rf/reg-sub ::counter-ids
            (fn [db _]
              (-> (get db ::counters)
                  (keys))))

;; 4. Views
;;    Use reagent to create views.
;;
;;    - use `re-frame.core/subscribe` to read queries
;;    - use `reagent/atom` for local state (not shown here)

(defn root-view
  "Render the page"
  []
  [:div
   {:style {:max-width 300
            :margin    "50px auto"
            :font-size 16}}
   "Click to count!"
   (let [counters (rf/subscribe [::counter-ids])]
     (doall (for [id @counters]
              ^{:key id} [counter id])))])

(defn ^:export render []
  (reagent/render [root-view]
                  (js/document.getElementById "shadow-re-frame")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize])

  (render))

