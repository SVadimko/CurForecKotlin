package com.vadimko.curforeckotlin.cbxmlapi

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * splitting response into classes
 */

@Root(name = "ValCurs", strict = false)
data class MOEXXMLResponse(
    @field:ElementList(name = "Record", inline = true)
    var record: List<Record>? = null,
)


@Root(name = "Record", strict = false)
data class Record(

    @field:Element(name = "Value")
    var Value: String = "0.0",

    @field:Attribute(name = "Date")
    var Date: String = "",
)
