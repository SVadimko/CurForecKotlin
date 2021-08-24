# Package com.vadimko.curforeckotlin

Main package, contains all application packages and classes The application uses the api Tinkov Bank,
the Central Bank of Russia and the Moscow Exchange to view the exchange rates of these companies.
Based on the data of the Moscow Exchange, a forecast is made for further changes in the exchange rate.
The forecast is based on three different calculation methods:

1) Moving average method
2) Exponential smoothing method
3) Least squares method

The application also allows you to monitor the exchange rate of Tinkov Bank at a specified interval
and notify the user if the rate exceeds / falls below the specified value. The exchange rates of
Tinkov Bank and the Central Bank can be displayed on the widget, on the desktop. The built-in
calculator will tell you how much it will cost to buy or sell a given amount of currency.

# Package com.vadimko.curforeckotlin.adapters

Contains CBmainAdapter, TCSmainAdapter - adapter classes for recycleview NowFragment

# Package com.vadimko.curforeckotlin.cbjsonApi

Contains set of classes for requesting data about currencies using Retrofit from the Central Bank
website (for the current and past days)
for the NowFragment fragment with a response in Json format

# Package com.vadimko.curforeckotlin.cbxmlApi

Contains set of classes for requesting currency data using Retrofit from the Moscow Exchange
website (for a user-specified period)
for TodayFragment and ArhiveFragment fragments with a response in Json format

# Package com.vadimko.curforeckotlin.tcsApi

Contains set of classes for requesting data using Retrofit from the Tinkov website (actual
currencies values) for a NowFragment fragment with a response in Json format

# Package com.vadimko.curforeckotlin.moexApi

Set of classes for requesting currency data using Retrofit from the Moscow Exchange
website (for a user-specified period) for TodayFragment and ArchiveFragment fragments with a response
in Json format

# Package com.vadimko.curforeckotlin.database

Contains set of classes for working with Sql database using Room (recording the results of updating
the currencies of the application widget into the database)

# Package com.vadimko.curforeckotlin.forecastsMethods

ExponentSmooth - calculation of the forecast by the method of exponential smoothing LessSquare -
Least Squares Forecast WMA - calculating the forecast by the moving average method

# Package com.vadimko.curforeckotlin.utils

Package with helper objects

# Package com.vadimko.curforeckotlin.ui

Contains UI packages

# Package com.vadimko.curforeckotlin.ui.archive

Contains View and ViewModel for Archive screen

# Package com.vadimko.curforeckotlin.ui.calc

Contains View and ViewModel for Calc screen

# Package com.vadimko.curforeckotlin.ui.now

Contains View and ViewModel for Now screen

# Package com.vadimko.curforeckotlin.ui.today

Contains View and ViewModel for Today screen

# Package com.vadimko.curforeckotlin.updateWorkers (not used)

Contains Workers to update the exchange rate information for the relevant screens

# Package com.vadimko.curforeckotlin.widget

Contains classes that implement the functionality of the widget


