```
INSERT INTO Countries (Name)
Values ("United States");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("United States Dollar","USD","$",true, 1792);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("United States", "USD");

INSERT INTO Countries (Name)
Values ("Canada");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Canadian Pound","CAP","£",true,1841, 1858);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("Canadian Dollar","CAD","$",true,1858);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Canada", "CAD");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Canada", "CAP");

INSERT INTO Countries (Name)
Values ("Mexico");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("Mexican Peso","MXN","$",true,1823);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Mexico", "MXN");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("Euro","EUR","€",true,1999);
        
INSERT INTO Countries (Name)
Values ("Germany");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("East German Deutsche Mark","DM","DM",true, 1951, 1964);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Mark der Deutschen Notenbank","MDN","MDN",true, 1964, 1967);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Mark der DDR","MDDR","M",true, 1968, 1990);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Deutsche Mark","DEM","DM",true, 1979, 1999);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Reichsmark","ℛℳ","ℛℳ",true, 1924, 1948);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("German Goldmark","Mark","ℳ",false, 1873, 1914);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("German Rentenmark","RM","RM",false,1923,1924);

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Allied-Military Currency","AM-Mark","Mark",true,1944, 1948);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "EUR");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "DEM");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "RM");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "DM");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "ℛℳ");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "AM-Mark");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "MDN");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "MDDR");

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Germany", "Mark");

INSERT INTO Countries (Name)
Values ("Afghanistan");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("Afghanistan Afghani","AFN","Afs",false,1925);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Afghanistan", "AFN");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart, YrEnd)
Values ("Afghanistan Rupee","AFR","R",false,1891,1925);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Afghanistan", "AFR");

INSERT INTO Countries (Name)
Values ("Albania");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("Albanian Lek","ALL","L",false,1926);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Albania", "ALL");

INSERT INTO Countries (Name)
Values ("Algeria");

INSERT INTO Currencies (Name, Abbreviation, Symbol, SymbolBefore, YrStart)
Values ("Algerian Dinar","DZD","دج",false,1926);

INSERT INTO CountryCurrencies (CountryName, CurrencyAbbr)
Values("Algeria", "DZD");
```