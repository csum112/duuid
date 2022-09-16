### Unique ID Generation
A solution for generating globally unique identifiers, similar to [Twitter Snowflake](https://en.wikipedia.org/wiki/Snowflake_ID).

#### Project Structure
* `./generator/` - main module - provides implementation & unit tests.
* `./benchmark/` - provides the code to generate the benchmark stats.
* `./application/` - minimalistic SpringBoot application for load testing.

#### Results

| Test Environment                      | Resources                                                  | Ids generated | Duration (sec) | Avg. Throughput |
|---------------------------------------|------------------------------------------------------------|---------------|----------------|-----------------|
| Synthetic Benchmark (30 samples)      | 16Gb Ram + 6 cores                                         | N/A           | N/A            | 224214,98 ids/s |
| Load Test Local (Springboot + Tomcat) | 16Gb Ram + 6 cores (Same machine server + jMeter instance) | 4913667       | 100            | 49136,67 req/s  |
| E2E AWS (Springboot + Tomcat)         | Server: t3.xlarge; jMeterinstances:  2 x t2.medium         | 2526895       | 100            | 25268,95 req/s  |