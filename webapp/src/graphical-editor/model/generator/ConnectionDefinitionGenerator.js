
class ConnectionDefinitionGenerator { 

    constructor() {
        this.placingShape = {
            'line': (shape) => this.generateLineShape(shape),
            'polyline': (shape) => this.generatePolyLineShape(shape),
            'rectangle': (shape, distance) => this.generateRectangleShape(shape, distance),
            'roundedRectangle': (shape, distance) => this.generateRoundedRectangleShape(shape, distance),
            'ellipse': (shape, distance) => this.generateEllipseShape(shape, distance),
            'text': (shape) => this.generateTextShape(shape)
        };
    }

    generate(connections) {
        let result = {}
        connections.map(function(connection) {
            result.push(createConnection(connection))
        })
        return result;
    }

    createPlacingList(connection) {
        const result = connection.placings.map(this.createPlacing, this);
        return result;
    }

    createPlacing(placing) {
        const generatedPlacing = {
            position: placing.positionOffset
        }
        return Object.assign(generatedPlacing, this.createPlacingShape(placing));
    }

    createPlacingShape(placing) {

        let placingShape = this.placingShape[placing.shape.type](placing.shape, placing.positionDistance);
        return placingShape;
    }

    generatePlacingShapeStyle(generatedShape, shape) {
        generatedShape.attrs = 'style' in shape ? Object.assign(generatedShape.attrs, {style: shape.style}): generatedShape.attrs;
        return generatedShape;
    }

    generateLineShape(line) {
        const shape = {
            markup: '<line />',
            attrs: {
                x1: line.startPoint.x,
                y1: line.startPoint.y,
                x2: line.endPoint.x,
                y2: line.endPoint.y
            }
        };
        return this.generatePlacingShapeStyle(shape, line)
    }

    generatePolyLineShape(shape) {
        const polyLineShape = {
            markup: '<polyline />',
            attrs: {
                points: this.generatePoints(shape.points),
                fill: 'transparent'
            }
        };  
        return this.generatePlacingShapeStyle(polyLineShape, shape)
        
    }

    generatePoints(points) {
        let pointString = "";
        points.map(function(point) {
            pointString += (`${point.x},${point.y} `)
        })
        return pointString.trim();
    }
    
    generateRectangleShape(rectangle, distance) {
        const shape = {
            markup: '<rect />',
            attrs:{
                height: rectangle.sizeHeight,
                width: rectangle.sizeWidth,
                y: distance - rectangle.sizeHeight / 2
            }
        };
        return this.generatePlacingShapeStyle(shape, rectangle)
        
    }
    
    generateRoundedRectangleShape(roundedRectangle, distance) {
        const shape = {
            markup: '<rect />',
            attrs:{
                height: roundedRectangle.sizeHeight,
                width: roundedRectangle.sizeWidth,
                rx: roundedRectangle.curveWidth,
                ry: roundedRectangle.curveHeight,
                y: distance - roundedRectangle.sizeHeight / 2,
            }
        }
        return this.generatePlacingShapeStyle(shape, roundedRectangle)        
      }
    
      /*
      private def generatePlacingShape(shape: Polygon, distance: Int) = {
        """
        markup: '<polygon />',
        attrs:{
          points: """" + shape.points.map(point => point.x + "," + point.y + { if (point != shape.points.last) " " else "\"" }).mkString + raw""",
          ${if (shape.style.isDefined) StyleGenerator.commonAttributes(shape.style.get) else ""}
        }
        """
      }*/
    
    generateEllipseShape(ellipse, distance) {
        const shape = {
            markup: '<ellipse />',
            attrs:{
                rx: ellipse.sizeWidth / 2,
                ry: ellipse.sizeHeight / 2,
                cy: distance,
            }
        }
        return this.generatePlacingShapeStyle(shape, ellipse);   
    }
    
    generateTextShape(text) {
        return {
            markup: `<text>${text.textBody}</text>`,
            attrs:{
                y: text.sizeHeight / 2
            }
        };
    }
    
    createLabelList(connection) {
        const labels = connection.placings.filter(placing => placing.shape.type === 'Label');
        return labels.map(this.createLabel);
    }

    createLabel(placing) {
        return {
            position: placing.positionOffset,
            attrs: {
              rect: {fill: 'transparent'},
              text: {
                y: 'positionDistance' in placing ? placing.positionDistance : 0,
                text: placing.shape.textBody
              }
            },
            id: placing.shape.id
        };
    }

}

export default class Generator{
    constructor(connections) {
        this.connectionDefinitionGenerator = new ConnectionDefinitionGenerator()
        this.connections = connections
    }

    getConnectionStyle(styleName) {
        
    }

    getPlacings(styleName) {
        const connection = this.connections.find(c => c.name === styleName);
        return connection ? this.connectionDefinitionGenerator.createPlacingList(connection) : [];
    }

    getLabels(styleName) {
        const connection = this.connections.find(c => c.name === styleName);
        return connection ? this.connectionDefinitionGenerator.createLabelList(connection) : [];
    }
}
