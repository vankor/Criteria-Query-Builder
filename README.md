# Criteria-Query-Builder

CQueryBuilder is a lib, that simplify building of criteria api Jpa queries. 
Core class of the lib is CQueryBuilder that allows to use builder pattern approach for constructing database queries.

Features, that we can use:

 - joins (left, right, inner)
 - where (Equals, Between. ontains and other)
 - and/or 
 - groub by
 - aggreagating functions (sum, min, max, count, avg)
 - Spring Data Pageable object for paging and sorting
 
 Lib allows to pass result set of constructed query, that used entity class into another no entity class, for example Dto. 
 We can map query result to other entity with help of passing method and constructor params or field annotations for no-entity class (@ResultProp). 

In usage example we have to join 3 tables, filter rows, group results and pass result into no-entity objects. 

```
EntityManager em = (.. getting EntityManager )
Pageable pageable = (.. getting Spring Data pageable object)


Page<BannerStatistic> bannerStats = CQueryBuilder.<BannerImpression, BannerStatistic>from(BannerImpression.class, em)
                    .leftJoin("clickout")
                    .innerJoin("banner")
                    .innerJoin("banner.placement")
                    .into(BannerStatistic.class)
                    .where("viewTime", Matchers.BETWEEN, fromDate, toDate)
                    .and("banner.partnerId", Matchers.EQUALS, login.getBrandId())
                    .groupBy("banner.id", "banner.placement.name")
                    .listPageable(pageable);
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
  
Entity class: 
```  
@Entity
@Table(name = "table_name")
class BannerImpression implements Impression {

    @Id
    @Column(name = "bi_hash")
    private String hash;

    @Column(name = "bi_parent_hash")
    private String parentHash;

    @ManyToOne
    @JoinColumn(name = "bi_banner_id", referencedColumnName = "b_id")
    private Banner banner;

    @Column(name = "bi_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime viewTime;

    @Column(name = "bi_bid_millicent")
    private Integer bidMillicent;

    @Column(name = "bi_country_id")
    private Integer countryId;

    @OneToOne(mappedBy = "parentImpression", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private BannerClickout clickout;
    
    ....
 }                     
```
