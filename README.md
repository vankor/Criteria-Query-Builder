# Criteria-Query-Builder

CQueryBuilder is a lib, that simplify building of criteria api Jpa queries. 
Core class of the lib is CQueryBuilder that allows to use builder pattern approach for constructing database queries.

Features, that we can use:

 - joins (left, right, inner)
 - where (Equals, Between. ontains and other)
 - and/or 
 - groub by
 - aggreagating functions (sum, min, max, count, avg)
 - spring data Pageable object for paging and sorting
 
 Lib allows to pass result set of constructed query, that used entity class into another no entity class, for example Dto. 
 We can map query result to other entity with help of passing method and constructor params or field annotations for no-entity class (@ResultProp). 

In usage example query we have to join 4 tables, filter rows, group results, pass result into no-entity object, aplly pagging. 
That provides banner click statistic.

  
Entity classes: 
```  
@Entity
@Table(name = "banner_imp")
class BannerImpression {

    @Id
    @Column(name = "hash")
    private String hash;

    @ManyToOne
    @JoinColumn(name = "banner_id", referencedColumnName = "id")
    private Banner banner;

    @Column(name = "view_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime viewTime;

    @Column(name = "bid_millicent")
    private Integer bidMillicent;

    @Column(name = "country_id")
    private Integer countryId;

    @OneToOne(mappedBy = "parentImpression", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private BannerClickout clickout;
    
    ....
 }

 
@Entity
@Table(name = "banner_click")
public class BannerClickout {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE ...)
    @Column(name = "...")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imp_hash")
    private BannerImpression parentImpression;

    @Column(name = "...")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime viewTime;
    
    ....
 }
 
@Entity
@Table(name = "banner")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE ....)
    @Column(name = "id")
    private Integer id;

    @Column(name = "...")
    private Integer partnerId;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="place_id", referencedColumnName = "id")
    private BannerPlacement placement;

    @Column(name = "...")
    private String src;

    @Column(name = "...")
    private String href;

    @Column(name = "...")
    private String alt;

    @Column(name = "...")
    private Integer bidMillicent;
  
    ....
 }
 
 @Entity
 @Table(name = "banner_place")
 public class BannerPlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE ...")
    @Column(name = "id")
    private Integer id;

    @Column(name = "...", unique = true)
    private String name;
    
    
    ....
 }   
    
 
```
   
Result Dto:

```
class BannerStatistic {

    @ResultProp(value = "banner.id", order = 1) // @ResultProp defines query result field mappings
    private Integer bannerId;

    @ResultProp(value = "banner.partnerId", order = 2)
    private Integer partnerId;

    @ResultProp(value = "banner.src", order = 3)
    private String src;

    @ResultProp(value = "banner.href", order = 4)
    private String href;

    @ResultProp(value = "banner.placement.name", order = 5)
    private String placement;

    @ResultProp(value = "COUNT(hash)", order = 6)
    private Long viewCount;

    @ResultProp(value = "COUNT(clickouts.id)", order = 7)
    private Long clickCount;

    @ResultProp(value = "AVG(bidMillicent)", order = 8)
    private Double averageMillicent;

    @ResultProp(value = "SUM(bidMillicent)", order = 9)
    private Long commonAmount;
    
    ....
    
  }       
```  

QueryBuilder final query:

```
EntityManager em = (.. getting EntityManager )
Pageable pageable = (.. getting Spring Data pageable object)


Page<BannerStatistic> bannerStats = CQueryBuilder.<BannerImpression, BannerStatistic>from(BannerImpression.class, em)
                    .leftJoin("clickout")
                    .innerJoin("banner")
                    .innerJoin("banner.placement")
                    .into(BannerStatistic.class)
                    .where("viewTime", Matchers.BETWEEN, //fromDate//, //toDate//)
                    .and("banner.partnerId", Matchers.EQUALS, //partnerId//)
                    .groupBy("banner.id", "banner.placement.name")
                    .listPageable(pageable);
```  

Finally CQueryBuilder will generate such analytical SQL query:

``` 
 SELECT alias3.id,
        alias3.partner_id,
        alias3.src,
        alias3.href,
        alias4.name,
        COUNT(alias1.hash),
        COUNT(alias2.id),
        AVG(alias1.bid_millicent),
        SUM(alias1.bid_millicent)
 FROM banner_imp alias1 LEFT JOIN banner_click alias2 ON alias1.hash = alias2.imp_hash
                        INNER JOIN banner alias3 ON alias1.banner_id = alias3.id
                        INNER JOIN banner_place alias4 ON alias3.id = alias4.id
      WHERE  (view_time BETWEEN //dateFrom// AND //dateTo//) 
             AND alias3.partner_id = //partnerId//
      GROUP BY alias3.id, alias4.name
      ORDER BY {orderColumn}  // order from Pageable object
      LIMIT {limit} // limit from Pageable object
``` 
