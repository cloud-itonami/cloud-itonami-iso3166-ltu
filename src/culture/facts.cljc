(ns culture.facts
  "Country-level regional-culture catalog for Lithuania (LTU) -- national
  dishes, protected products, beverages, crafts, festivals and heritage
  sites, per ADR-2607171400 addendum 2 (cloud-itonami-municipality-
  culture-catalog Wave 1, in com-junkawasaki/root). Sibling namespace to
  `marketentry.facts` / `statute.facts` (ADR-2607141700); city-level
  counterparts live in the cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"LTU"
   [{:culture/id "ltu.dish.cepelinai"
     :culture/name "Cepelinai"
     :culture/country "LTU"
     :culture/kind :dish
     :culture/summary "Lithuanian national dish of potato dumplings made from grated potatoes and stuffed with ground meat, dry curd cheese, liver, or mushrooms."
     :culture/url "https://en.wikipedia.org/wiki/Cepelinai"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.dish.saltibarsciai"
     :culture/name "Cold beet soup"
     :culture/name-local "Šaltibarščiai"
     :culture/country "LTU"
     :culture/kind :dish
     :culture/summary "Cold summer soup found in the cuisines of Belarus, Lithuania, Latvia, Poland and Ukraine; the Lithuanian version is widely regarded as one of the country's best-known summer dishes."
     :culture/url "https://en.wikipedia.org/wiki/Cold_beet_soup"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.dish.kugelis"
     :culture/name "Kugelis"
     :culture/country "LTU"
     :culture/kind :dish
     :culture/summary "Traditional Lithuanian potato pudding made with potatoes, bacon, milk, onions and eggs, oven-baked and typically served with sour cream or pork rinds."
     :culture/url "https://en.wikipedia.org/wiki/Kugelis"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.beverage.midus"
     :culture/name "Midus"
     :culture/country "LTU"
     :culture/kind :beverage
     :culture/summary "Lithuanian mead, an alcoholic beverage made of grain, honey and water that Balts have been producing for thousands of years."
     :culture/url "https://en.wikipedia.org/wiki/Midus"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.product.amber"
     :culture/name "Lithuanian amber"
     :culture/country "LTU"
     :culture/kind :product
     :culture/summary "Fossilized resin significant to Lithuanian culture and craftsmanship; the Palanga Amber Museum, a branch of the Lithuanian National Museum of Art, houses about 4,500 amber pieces including historical artifacts, jewelry and modern works by Lithuanian craftsmen."
     :culture/url "https://en.wikipedia.org/wiki/Palanga_Amber_Museum"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.craft.cross-crafting"
     :culture/name "Lithuanian cross crafting"
     :culture/name-local "Kryždirbystė"
     :culture/country "LTU"
     :culture/kind :craft
     :culture/summary "Traditional Lithuanian art of crafting crosses combining Christian and Baltic mythological motifs, typically carved from oak wood by skilled craftsmen known as kryždirbiai."
     :culture/url "https://en.wikipedia.org/wiki/Lithuanian_cross_crafting"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.festival.jonines"
     :culture/name "Saint Jonas's Festival"
     :culture/name-local "Joninės"
     :culture/country "LTU"
     :culture/kind :festival
     :culture/summary "Lithuanian midsummer folk festival celebrated on 24 June, combining Christian observance of Saint John the Baptist's feast day with pagan summer solstice traditions."
     :culture/url "https://en.wikipedia.org/wiki/Saint_Jonas%27s_Festival"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.heritage.curonian-spit"
     :culture/name "Curonian Spit"
     :culture/country "LTU"
     :culture/kind :heritage
     :culture/summary "98-kilometre-long thin, curved sand-dune spit separating the Curonian Lagoon from the Baltic Sea; a UNESCO World Heritage Site shared by Lithuania and Russia."
     :culture/url "https://en.wikipedia.org/wiki/Curonian_Spit"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "ltu.heritage.vilnius-old-town"
     :culture/name "Vilnius Historic Centre"
     :culture/country "LTU"
     :culture/kind :heritage
     :culture/summary "Historic centre of Vilnius, inscribed as UNESCO World Heritage Site No. 541 in 1994 in recognition of its universal value and originality."
     :culture/url "https://en.wikipedia.org/wiki/Vilnius_Old_Town"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-ltu culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "LTU"))
                 " LTU entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
