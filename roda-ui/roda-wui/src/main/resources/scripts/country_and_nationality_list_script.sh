#!/bin/bash

# List of countries and nationality are downloaded from GitHub: https://github.com/umpirsky/country-list

wget https://raw.githubusercontent.com/umpirsky/country-list/master/data/$1/country.csv
tail -n +2 country.csv | sort | cut -d, -f2 | tr -d "\"" | awk '{printf("countryList[%d]: %s\n", NR-1, $0)}' > country_$1.properties

wget https://raw.githubusercontent.com/umpirsky/language-list/master/data/$1/language.csv
tail -n +2 language.csv | sort | cut -d, -f2 | tr -d "\"" | awk '{printf("nationalityList[%d]: %s\n", NR-1, $0)}' > language_$1.properties

rm country.csv
rm language.csv