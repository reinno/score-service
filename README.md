# Score Service

[![Build Status](https://travis-ci.org/reinno/score-service.svg?branch=master)](https://travis-ci.org/reinno/score-service)


## How to
### Build
```bash
cd ./score-service
sbt assembly
java -jar ./target/scala-2.11/score-service-1.0.jar
```

### Add a Rule
- implement rule actor with trait RuleService
- add the rule actor props func to RuleService Props Factory
- add rule configuration


## Design


```
                                                                                                               
                                                                                                               
                        ┌─────────────────┐                                                                    
                        │                 │                                                                    
                        │   Actor System  │                                                                    
                        │      /usr       │                                                                    
                        │                 │                                                                    
                        └─────────────────┘                                                                    
                                 │                                                                             
                                 │                                                                             
               ┌─────────────────┴─────────────────────┐                                                       
               │                                    1  │                                                       
      ┌────────▼────────┐                     ┌────────▼────────┐                                              
      │                 ├┐                    │                 │                                              
      │    akka-http    ││                    │      score      │                                              
      │      route      ││                    │     service     │                                              
      │                 ││                    │                 │                                              
      └┬────────────────┘│                    └─────────────────┘                                              
       └─────────────────┘                         1   │                                                       
                                                       │                                                       
                                   ┌───────────────────┼───────────────────┐                                   
                                   │                   │                   │                                   
                                 1 │                 1 │                   │                                   
                          ┌────────▼────────┐┌─────────▼───────┐┌──────────▼──────┐         ┌─────────────────┐
                          │                 ││                 ││                 ├─┐       │                 │
                          │  country-rule   ││   hotel-rule    ││   other-rule    │ │       │      data       │
                          │     service     ││     service     ││     service     │ │       │     service     │
                          │                 ││                 ││                 │ │       │                 │
                          └────────┬────────┘└─────────────────┘└─┬───────────────┘ │       └─────────────────┘
                                                                  └─────────────────┘                ▲         
                                   │                                                                           
                          ┌ ─ ─ ─ ─▼─ ─ ─ ─ ┐                                                        │         
                                                                                                               
                          │     refresh     │                                                        │         
                                worker                                                                         
                          │                 │                                                        │         
                           ─ ─ ─ ─ ─ ─ ─ ─ ─                                                                   
                                   ▲                                                                 │         
                                                                                                               
                                   └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ HTTP─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘         
                                                                                                               
                                                                                                               
                                                                                                               
                                                                                                               
                                                                                                               
                   │ │                                                                                         
                   │ │                                                                                         
                   │ │                                                                                         
              HttpRequest                                                                                      
                   │ │                                                                                         
                   │ │                                                                                         
           ┌───────▼─────────┐                            ┌─────────────────┐                                  
           │                 ├┐       Score               │                 │                                  
           │    akka-http    ├┼──────Request──────────────▶     Score       │                                  
           │      route      ││                           │    Service      │────────────────────────┐         
           │                 ◀┼───┐                       │                 │                        │         
           └┬──────┬─────────┘│   │                       └─────────────────┘                        │         
            └──────┼─┬────────┘   │                                                                  │         
                   │ │            │                                                                  │         
             HttpResponse         │                                                                  │         
                   │ │            │     ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐  │         
                   │ │            │     │                 │ │                 │ │                 │  │         
                   ▼ ▼            │     │    other-rule   │ │    hotel-rule   │ │  country-rule   │  │         
                                  └─────│     service     ◀─┤     service     ◀─┤     service     │◀─┘         
                                        │     ◷           │ │     ◷           │ │     ◷           │            
                                        └──────┬─▲────────┘ └──────┬─▲────────┘ └──────┬─▲────────┘            
                                               │ │                 │ │                 │ │                     
                                               │ │                 │ │                 │ │                     
                                            Refresh             Refresh             Refresh                    
                                               │ │                 │ │                 │ │                     
                                               │ │                 │ │                 │ │                     
                                               ▼ │                 ▼ │                 ▼ │                                             
```

Use a local http service simulate outside resources, hotel and country rule will send http request to refresh new
dates.

Unit test also use http client mock to test the behaves of rule actors.

Use single actor for one rule now, it can be extended to router-routee model if needed. 



## Restful API
  
### Score

* **URL**

  /api/v1/score

* **Method:**
  
  `POST`
  
*  **URL Params**

   N/A

* **Data Params**

List of structure

|name     |type  |mandatory|description
|---------|------|---------|-----------
|hotelId  |Int   |yes      |
|countryId|Int   |yes      |

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** 
    
List of structure

|name     |type  |mandatory|description
|---------|------|---------|-----------
|hotelId  |Int   |yes      |
|score    |Double|yes      |
 
* **Error Response:**

  * **Code:** 500 Internal failure <br />

  
* **Sample Call:**

```bash
curl -i  -X POST  -H "Content-Type:application/json" -d '[{"hotelId": 2, "countryId": 2}]' http://127.0.0.1:8002/api/v1/score
HTTP/1.1 200 OK
Server: akka-http/2.4.10
Date: Fri, 31 Mar 2017 10:16:54 GMT
Content-Type: application/json
Content-Length: 27

[{"hotelId":2,"score":3.0}]
```

### Rule

* **URL**

  /api/v1/rule/{rule_name}/enable

* **Method:**
  
  `POST`
  
*  **URL Params**

|name     |type  |mandatory|description
|---------|------|---------|-----------
|rule_name|string|yes      |

* **Data Params**

N/A

* **Success Response:**

  * **Code:** 200 <br />
 
* **Error Response:**

  * **Code:** 404 Rule not found <br />

  
* **Sample Call:**

```bash
curl -i  -X POST  -H "Content-Type:application/json"  http://127.0.0.1:8002/api/v1/rule/special-hotel/enable
```


* **URL**

  /api/v1/rule/{rule_name}/disable

* **Method:**
  
  `POST`
  
*  **URL Params**

|name     |type  |mandatory|description
|---------|------|---------|-----------
|rule_name|string|yes      |

* **Data Params**

N/A

* **Success Response:**

  * **Code:** 200 <br />
 
* **Error Response:**

  * **Code:** 404 Rule not found <br />

  
* **Sample Call:**

```bash
curl -i  -X POST  -H "Content-Type:application/json"  http://127.0.0.1:8002/api/v1/rule/special-hotel/disable
```


## Test
```bash
echo '[{"hotelId": 3, "countryId": 2}]' > post.json
ab  -l -n 10000 -c 100  -T 'application/json' -p post.json http://127.0.0.1:8002/api/v1/score
```

