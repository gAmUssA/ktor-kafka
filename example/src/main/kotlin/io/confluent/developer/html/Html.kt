package io.confluent.developer.html

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import org.w3c.dom.Document

object Html {

    class TEMPLATE(consumer: TagConsumer<*>) :
        HTMLTag(
            "template", consumer, emptyMap(),
            inlineTag = true,
            emptyTag = false
        ), HtmlInlineTag

    fun FlowContent.template(block: TEMPLATE.() -> Unit = {}) {
        TEMPLATE(consumer).visit(block)
    }

    fun TEMPLATE.li(classes: String? = null, block: LI.() -> Unit = {}) {
        LI(attributesMapOf("class", classes), consumer).visit(block)
    }

    fun page(js: String, content: FlowContent.() -> Unit = {}): HTML.() -> Unit = {
        head {
            css("https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css")
            css("https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css")
            js("https://code.jquery.com/jquery-3.5.1.slim.min.js")
            js("https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js")

            js("/assets/$js")
            title("Ktor Kafka App")
        }

        body {
            div("container rounded") {
                content()
            }
        }
    }

    val indexHTML = page("index.js") {
        val movies = mapOf(
            362 to "Lethal Weapon",
            363 to "Guardians of the Galaxy",
            364 to "Se7en"
        )
        div("row") {
            form(
                action = "/rating",
                method = FormMethod.post
            ) {
                name = "myform"
                id = "myform"
                div("form-group row") {
                    label("col-4 col-form-label") {
                        htmlFor = "movieId"
                        +"Movie Title"
                    }
                    div("col-8") {
                        select("custom-select") {
                            name = "movieId"
                            id = "movieId"
                            for ((k, v) in movies) {
                                option {
                                    value = k.toString()
                                    +v
                                }
                            }
                        }
                    }
                }

                div("form-group row") {
                    label("col-4 col-form-label") {
                        htmlFor = "rating"
                        +"Rating"
                    }
                    div("col-8") {
                        select("custom-select") {
                            name = "rating"
                            id = "rating"
                            for (n in 10 downTo 1) {
                                option {
                                    value = n.toString()
                                    +"$n"
                                }
                            }
                        }
                    }
                }

                div("form-group row") {
                    div("offset-4 col-8") {
                        button(classes = "btn btn-primary", type = ButtonType.submit, name = "submit") {
                            +"Submit"
                        }

                    }
                }

            }
        }

        div("container") {
            id = "myAlert"
            div("alert alert-success alert-dismissible hide") {
                id = "myAlert2"
                role = "alert"
                +"Thank you for submitting your rating"
                button(type = ButtonType.button, classes = "close") {
                    attributes["data-dismiss"] = "alert"
                    span {
                        +"x"
                    }
                }
            }
        }


    }

    val index: Document = createHTMLDocument().html(block = indexHTML)

    fun HEAD.css(source: String) {
        link(source, LinkRel.stylesheet)
    }

    fun HEAD.js(source: String) {
        script(ScriptType.textJavaScript) {
            src = source
        }
    }
}
