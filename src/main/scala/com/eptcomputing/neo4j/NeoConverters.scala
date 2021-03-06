package com.eptcomputing.neo4j

import org.neo4j.api.core._

/**
 * Extend your class with this trait to get really neat new notation for creating
 * new relationships. For example, ugly Java-esque code like:
 * <pre>
 * val knows = DynamicRelationshipType.withName("KNOWS")
 * start.createRelationshipTo(intermediary, knows)
 * intermediary.createRelationshipTo(end, knows)
 * </pre>
 *
 * can be replaced with a beautiful Scala one-liner:
 * <pre>start --| "KNOWS" --> intermediary --| "KNOWS" --> end</pre>
 *
 * Feel free to use this example to tell all your friends how awesome scala is :)
 */
trait NeoConverters {

  class NodeRelationshipMethods(node: Node) {

    def -->(relType: RelationshipType) = new OutgoingRelationshipBuilder(node, relType)

    // Create incoming relationship

    def <--(relType: RelationshipType) = new IncomingRelationshipBuilder(node, relType)
  }

  // Half-way through building an outgoing relationship
  class OutgoingRelationshipBuilder(fromNode: Node, relType: RelationshipType) {
    def -->(toNode: Node) = {
      fromNode.createRelationshipTo(toNode, relType)
      new NodeRelationshipMethods(toNode)
    }
  }

  // Half-way through building an incoming relationship
  class IncomingRelationshipBuilder(toNode: Node, relType: RelationshipType) {
    def <--(fromNode: Node) = {
      fromNode.createRelationshipTo(toNode, relType)
      new NodeRelationshipMethods(fromNode)
    }
  }

  implicit def node2relationshipBuilder(node: Node) = new NodeRelationshipMethods(node)

  implicit def string2RelationshipType(relType: String) = DynamicRelationshipType.withName(relType)

  class RichPropertyContainer(propertyContainer: PropertyContainer) {
    def apply(property: String) : Option[Any] = if(propertyContainer.hasProperty(property)) Some(propertyContainer.getProperty(property)) else None
    def update(property: String, value: Any) : Unit = propertyContainer.setProperty(property, value)
  }

  implicit def propertyContainer2RichPropertyContainer(propertyContainer: PropertyContainer) = new RichPropertyContainer(propertyContainer)

  implicit def fn2StopEvaluator(e : TraversalPosition => Boolean) = 
    new StopEvaluator() {
      def isStopNode(traversalPosition : TraversalPosition) = e(traversalPosition)
    }

  implicit def fn2ReturnableEvaluator(e : TraversalPosition => Boolean) = 
    new ReturnableEvaluator () {
      def isReturnableNode(traversalPosition : TraversalPosition) = e(traversalPosition)
    }
}
