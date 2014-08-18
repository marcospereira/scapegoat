package com.sksamuel.scapegoat.inspections.collections

import com.sksamuel.scapegoat._

/** @author Stephen Samuel */
class DuplicateMapKey extends Inspection {

  def inspector(context: InspectionContext): Inspector = new Inspector(context) {
    override def traverser = new context.Traverser {

      import context.global._

      private val Arrow = TermName("$minus$greater")
      private val UnicodeArrow = TermName("$u2192")

      private def isDuplicateKeys(trees: List[Tree]): Boolean = {
        val unique = trees.foldLeft(Set[String]())((set, tree) => tree match {
          case Apply(TypeApply(Select(Apply(_, args), Arrow | UnicodeArrow), _), _) =>
            set + args.head.toString()
          case _ => set
        })
        unique.size < trees.size
      }

      private def warn(tree: Tree) = {
        context.warn("Duplicated map key", tree.pos, Levels.Warning,
          "A map key is overwriten by a later entry: " + tree.toString().take(100), DuplicateMapKey.this)
      }

      override def inspect(tree: Tree): Unit = {
        tree match {
          case Apply(TypeApply(Select(Select(_, TermName("Map")), TermName("apply")), _),
          args) if isDuplicateKeys(args) =>
            warn(tree)
          case _ => continue(tree)
        }
      }
    }
  }
}
