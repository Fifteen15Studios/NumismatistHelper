```
INSERT INTO Countries (Name)
Values ("United States");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("United States Dollar","USD","$",true);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("United States", "USD", 1792);

INSERT INTO Countries (Name)
Values ("Canada");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Canadian Pound","CAP","£",true);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Canadian Dollar","CAD","$",true);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("Canada", "CAD",1858);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Canada", "CAP",1841, 1858);

INSERT INTO Countries (Name)
Values ("Mexico");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Mexican Peso","MXN","$",true);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("Mexico", "MXN",1823);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Euro","EUR","€",true);
        
INSERT INTO Countries (Name)
Values ("Germany");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("East German Deutsche Mark","DM","DM",true);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Mark der Deutschen Notenbank","MDN","MDN",true);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Mark der DDR","MDDR","M",true);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Deutsche Mark","DEM","DM",true);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Reichsmark","ℛℳ","ℛℳ",true);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("German Goldmark","Mark","ℳ",false);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("German Rentenmark","RM","RM",false);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Allied-Military Currency","AM-Mark","Mark",true);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("Germany", "EUR", 1999);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "DEM", 1979, 1999);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "RM",1923,1924);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "DM", 1951, 1964);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "ℛℳ", 1924, 1948);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "AM-Mark",1944, 1948);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "MDN", 1964, 1967);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "MDDR", 1968, 1990);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Germany", "Mark", 1873, 1914);

INSERT INTO Countries (Name)
Values ("Afghanistan");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Afghanistan Afghani","AFN","Afs",false);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("Afghanistan", "AFN",1925);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Afghanistan Rupee","AFR","R",false);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart, YrEnd)
Values("Afghanistan", "AFR",1891,1925);

INSERT INTO Countries (Name)
Values ("Albania");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Albanian Lek","ALL","L",false);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("Albania", "ALL",1926);

INSERT INTO Countries (Name)
Values ("Algeria");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore)
Values ("Algerian Dinar","DZD","دج",false);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr, YrStart)
Values("Algeria", "DZD",1926);
```