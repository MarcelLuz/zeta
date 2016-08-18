package generator.generators.style

/**
  * Created by julian on 07.10.15.
  * the generator object for style.js
  */

import generator.model.style._
import generator.model.style.color.Transparent
import generator.model.style.gradient.{Gradient, HORIZONTAL}
import java.nio.file.{Paths, Files}


object StyleGenerator {


  def filename = "style.js"

  def doGenerate(styles: List[Style], outputLocation: String) = {
    val output =
      s"""
        ${generateGetStyle(styles)}
        ${generateGetDiagramHighlighting(styles)}
      """
    Files.write(Paths.get(outputLocation + filename), output.getBytes)
  }

  def generateGetStyle(styles: List[Style]): String = {
    s"""
        function getStyle(stylename) {
          var style;
          switch(stylename) {
            ${styles.map(style => generateStyleCase(style)).mkString("")}
          default:
            style = {};
            break;
          }
          return style;
        }
    """
  }

  def generateStyleCase(s: Style) =
    s"""
      case '${s.name}':
        style = {
        ${createFontAttributes(s)}
        ${commonAttributes(s)}
      };
      break;
    """

  def generateGetDiagramHighlighting(styles: List[Style]): String = {
    s"""
      function getDiagramHighlighting(stylename) {
        var highlighting;
        switch(stylename) {
          ${styles.map(style => generateDiagramHighlightingCases(style)).mkString("")}
          default:
            highlighting = '';
          break;
        }
        return highlighting;
      }
    """
  }

  def generateDiagramHighlightingCases(s: Style) = {
    val highlighting = s"""${getSelected(s)}${getMultiselected(s)}${getAllowed(s)}${getUnallowed(s)}"""
    if (!highlighting.isEmpty)
      raw"""case "${s.name}":

              var highlighting = '$highlighting';

            break;
      """
    else ""
  }

  def getSelected(s: Style) =
    if (s.selected_highlighting isDefined) {
      raw""".paper-container .free-transform { border: 1px dashed  ${s.selected_highlighting.get.getRGBValue}; }"""
    } else ""

  def getMultiselected(s: Style) =
    if (s.multiselected_highlighting isDefined) {
      raw""".paper-container .selection-box { border: 1px solid ${s.multiselected_highlighting.get.getRGBValue}; }"""
    } else ""

  def getAllowed(s: Style): String =
    if (s.allowed_highlighting isDefined) {
      raw""".paper-container .linking-allowed { outline: 2px solid ${s.allowed_highlighting.get.getRGBValue}; }"""
    } else ""

  def getUnallowed(s: Style): String =
    if (s.unallowed_highlighting isDefined) {
      raw""".paper-container .linking-unallowed { outline: 2px solid ${s.unallowed_highlighting.get.getRGBValue}; }"""
    } else ""


  def createFontAttributes(s: Style) =
    s"""
          /*
          Generated Text style attributes
          */
            text: {
              ${fontAttributes(s)}
            },
    """

  def fontAttributes(s: Style) = {
    raw"""
       'dominant-baseline': "text-before-edge",
       'font-family': '${s.font_name.getOrElse("sans-serif")}',
       'font-size': '${s.font_size.getOrElse("11")}',
       'fill': '${val c = s.font_color; if (c.isDefined) c.get.getRGBValue else "#000000"}',
       'font-weight': ' ${if (s.font_bold.isDefined && s.font_bold.get) "bold" else "normal"}'
       ${if (s.font_italic.getOrElse(false)) raw""",'font-style': 'italic' """ else ""}
       """
  }

  def commonAttributes(s: Style): String = {
    raw"""
      ${if (checkBackgroundGradientNecessary(s))
          createGradientAttributes(s.background_color.get.asInstanceOf[Gradient],
            s.gradient_orientation.get match {
              case HORIZONTAL => true
              case _ => false
            })
        else
          createBackgroundAttributes(s)
      }
      'fill-opacity':${s.transparency.getOrElse("1.0")},
      ${createLineAttributesFromLayout(s)}
    """
  }

  def checkBackgroundGradientNecessary(s: Style) = if (s.background_color.isDefined && s.background_color.get.isInstanceOf[Gradient]) true else false

  def createGradientAttributes(gr: Gradient, horizontal: Boolean) = {
      s"""
      fill: {
        type: 'linearGradient',
        stops: [
          ${gr.area.map(area =>
              s"offset: '${(area.offset * 100).toInt}%', color: '${area.color.getRGBValue}'")
              .mkString("{", "\n}, {", "}")
          }
        ]
        ${if(horizontal) "" else
          """,attrs: { x1: '0%', y1: '0%', x2: '0%', y2: '100%'}"""}
      },
      """
  }

  def createBackgroundAttributes(s: Style): String = {
    if (s.background_color.isDefined) {
      val bg_color = s.background_color.get
      raw"""
          fill: '${bg_color.getRGBValue}',
        """
    }
    else ""
  }

  def createLineAttributesFromLayout(s: Style) = {
    var ret = """"""
    if (s.line_color.isEmpty)
      ret +=
        """
      				stroke: '#000000',
      				'stroke-width': 0,
      				'stroke-dasharray': "0"
        """
    else
      s.line_color.get match {
        case Transparent => ret +=
          """
                                        'stroke-opacity': 0,
          """
        case _ => ret +=
          """
      				stroke: '""" + s.line_color.get.getRGBValue +
          """'"""
          if (s.line_width.isDefined)
            ret += """,'stroke-width':""" + s.line_width.get
          if (s.line_style isDefined)
            s.line_style.get match {
              case DASH => ret +=
                """
                                  ,'stroke-dasharray': "10,10"
                """
              case DOT => ret +=
                """
                                  ,'stroke-dasharray': "5,5
                """
              case DASHDOT => ret +=
                """
                                  ,'stroke-dasharray': "10,5,5,5"
                """
              case DASHDOTDOT => ret +=
                """
                                  ,'stroke-dasharray': "10,5,5,5,5,5"
                """
              case _ => ret +=
                """
                                  ,'stroke-dasharray': "0"
                """
            }
      }
    ret
  }
}
