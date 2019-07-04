# About `jguru-shared-persistence-spi-jdbc`

The Shared Persistence JDBC SPI provides shallow/non-invasive utilities for interacting with SQL databases
without using JPA. 

The SPI contains algorithms to simplify the following operations:

1. **Synthesis**: Generating SQL statements by tokenizing SQL template strings.
2. **Common Template Storage Format**: Utilities to store SQL templates, parameters and classification in 
   a common  format.
3. **Common Substitution Tokens**: Define standard substitution tokens.
4. **Algorithms**: Defines some commonly used algorithms.

The typical use of the Persistence JDBC SPI is illustrated in the examples below and in the unit tests of this project.
