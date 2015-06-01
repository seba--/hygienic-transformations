package lang.java

import com.sun.source.tree._
import com.sun.tools.javac.tree.JCTree._
import com.sun.tools.javac.tree.{JCTree, TreeCopier, TreeMaker}
import com.sun.tools.javac.util
import com.sun.tools.javac.util.ListBuffer

class TrackingTreeCopier[P](M: TreeMaker) extends TreeVisitor[JCTree, P] {
  private var _originTracking = Map[JCTree, JCTree]()
  private var _permittedCapture = Map[JCTree, JCTree]()

  def originMap = _originTracking
  def permittedCapture = _permittedCapture

  def setOrigin[T <: JCTree](t: T, origin: JCTree): T = {
    _originTracking += t -> origin
    t
  }

  def captured[T <: JCTree](t: T, capturingDecs: JCTree*) = {
    for (capturingDec <- capturingDecs)
      _permittedCapture += t -> capturingDec
    t
  }

  def capturing[T <: JCTree](t: T, capturedRefs: JCTree*) = {
    for (capturedRef <- capturedRefs)
      _permittedCapture += t -> capturedRef
    t
  }


  def copy[T <: JCTree](node: T): T = {
    this.copy(node.asInstanceOf[T], null.asInstanceOf[P])
  }

  def copy[T <: JCTree](node: T, p: P): T = {
    if (node == null) null.asInstanceOf[T] else node.accept(this, p).asInstanceOf[T]
  }

  def copy[T <: JCTree](node: util.List[T]): util.List[T] = {
    this.copy(node, null.asInstanceOf[P])
  }

  def copy[T <: JCTree](nodes: util.List[T], p: P): util.List[T] = {
    if (nodes == null) {
      null
    } else {
      val buf = new ListBuffer[T]()
      val it = nodes.iterator
      while (it.hasNext) {
        val var5 = it.next()
        buf.append(this.copy(var5, p))
      }
      buf.toList()
    }
  }

  override def visitAnnotatedType(node: AnnotatedTypeTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCAnnotatedType]
    val annotations = this.copy(t.annotations, p)
    val typ = this.copy(t.underlyingType, p)
    setOrigin(this.M.at(t.pos).AnnotatedType(annotations, typ), t)
  }

  override def visitAnnotation(node: AnnotationTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCAnnotation]
    val annotationType = copy(t.annotationType, p)
    val args = copy(t.args, p)
    setOrigin(M.at(t.pos).Annotation(annotationType, args), t)
  }

  override def visitAssert(node: AssertTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCAssert]
    val cond = copy(t.cond, p)
    val detail = copy(t.detail, p)
    setOrigin(M.at(t.pos).Assert(cond, detail), t)
  }

  override def visitAssignment(node: AssignmentTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCAssign]
    val lhs = copy(t.lhs, p)
    val rhs = copy(t.rhs, p)
    setOrigin(M.at(t.pos).Assign(lhs, rhs), t)
  }

  override def visitCompoundAssignment(node: CompoundAssignmentTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCAssignOp]
    val lhs = copy(t.lhs, p)
    val rhs = copy(t.rhs, p)
    setOrigin(M.at(t.pos).Assignop(t.getTag, lhs, rhs), t)
  }

  override def visitBinary(node: BinaryTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCBinary]
    val lhs = copy(t.lhs, p)
    val rhs = copy(t.rhs, p)
    setOrigin(M.at(t.pos).Binary(t.getTag, lhs, rhs), t)
  }

  override def visitBlock(node: BlockTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCBlock]
    val stats = copy(t.stats, p)
    setOrigin(M.at(t.pos).Block(t.flags, stats), t)
  }

  override def visitBreak(node: BreakTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCBreak]
    setOrigin(M.at(t.pos).Break(t.label), t)
  }

  override def visitCase(node: CaseTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCCase]
    val pat = copy(t.pat, p)
    val stats = copy(t.stats, p)
    setOrigin(M.at(t.pos).Case(pat, stats), t)
  }

  override def visitCatch(node: CatchTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCCatch]
    val param = copy(t.param, p)
    val body = copy(t.body, p)
    setOrigin(M.at(t.pos).Catch(param, body), t)
  }

  override def visitClass(node: ClassTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCClassDecl]
    val mods = copy(t.mods, p)
    val typarams = copy(t.typarams, p)
    val extending = copy(t.extending, p)
    val implementing = copy(t.implementing, p)
    val defs = copy(t.defs, p)
    setOrigin(M.at(t.pos).ClassDef(mods, t.name, typarams, extending, implementing, defs), t)
  }

  override def visitConditionalExpression(node: ConditionalExpressionTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCConditional]
    val cond = copy(t.cond, p)
    val truepart = copy(t.truepart, p)
    val falsepart = copy(t.falsepart, p)
    setOrigin(M.at(t.pos).Conditional(cond, truepart, falsepart), t)
  }

  override def visitContinue(node: ContinueTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCContinue]
    setOrigin(M.at(t.pos).Continue(t.label), t)
  }

  override def visitDoWhileLoop(node: DoWhileLoopTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCDoWhileLoop]
    val body = copy(t.body, p)
    val cond = copy(t.cond, p)
    setOrigin(M.at(t.pos).DoLoop(body, cond), t)
  }

  override def visitErroneous(node: ErroneousTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCErroneous]
    val errs = copy(t.errs, p)
    setOrigin(M.at(t.pos).Erroneous(errs), t)
  }

  override def visitExpressionStatement(node: ExpressionStatementTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCExpressionStatement]
    val expr = copy(t.expr, p)
    setOrigin(M.at(t.pos).Exec(expr), t)
  }

  override def visitEnhancedForLoop(node: EnhancedForLoopTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCEnhancedForLoop]
    val `var` = copy(t.`var`, p)
    val expr = copy(t.expr, p)
    val body = copy(t.body, p)
    setOrigin(M.at(t.pos).ForeachLoop(`var`, expr, body), t)
  }

  override def visitForLoop(node: ForLoopTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCForLoop]
    val init = copy(t.init, p)
    val cond = copy(t.cond, p)
    val step = copy(t.step, p)
    val body = copy(t.body, p)
    setOrigin(M.at(t.pos).ForLoop(init, cond, step, body), t)
  }

  override def visitIdentifier(node: IdentifierTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCIdent]
    setOrigin(M.at(t.pos).Ident(t.name), t)
  }

  override def visitIf(node: IfTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCIf]
    val cond = copy(t.cond, p)
    val thenpart = copy(t.thenpart, p)
    val elsepart = copy(t.elsepart, p)
    setOrigin(M.at(t.pos).If(cond, thenpart, elsepart), t)
  }

  override def visitImport(node: ImportTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCImport]
    val qualid = copy(t.qualid, p)
    setOrigin(M.at(t.pos).Import(qualid, t.staticImport), t)
  }

  override def visitArrayAccess(node: ArrayAccessTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCArrayAccess]
    val indexed = copy(t.indexed, p)
    val index = copy(t.index, p)
    setOrigin(M.at(t.pos).Indexed(indexed, index), t)
  }

  override def visitLabeledStatement(node: LabeledStatementTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCLabeledStatement]
    val body = copy(t.body, p)
    setOrigin(M.at(t.pos).Labelled(t.label, t.body), t)
  }

  override def visitLiteral(node: LiteralTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCLiteral]
    setOrigin(M.at(t.pos).Literal(t.typetag, t.value), t)
  }

  override def visitMethod(node: MethodTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCMethodDecl]
    val mods = copy(t.mods, p)
    val restype = copy(t.restype, p)
    val typarams = copy(t.typarams, p)
    val params = copy(t.params, p)
    val thrown = copy(t.thrown, p)
    val body = copy(t.body, p)
    val defaultValue = copy(t.defaultValue, p)
    setOrigin(M.at(t.pos).MethodDef(mods, t.name, restype, typarams, params, thrown, body, defaultValue), t)
  }

  override def visitMethodInvocation(node: MethodInvocationTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCMethodInvocation]
    val typeargs = copy(t.typeargs, p)
    val meth = copy(t.meth, p)
    val args = copy(t.args, p)
    setOrigin(M.at(t.pos).Apply(typeargs, meth, args), t)
  }

  override def visitModifiers(node: ModifiersTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCModifiers]
    val annotations = copy(t.annotations, p)
    setOrigin(M.at(t.pos).Modifiers(t.flags, annotations), t)
  }

  override def visitNewArray(node: NewArrayTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCNewArray]
    val elemtype = copy(t.elemtype, p)
    val dims = copy(t.dims, p)
    val elems = copy(t.elems, p)
    setOrigin(M.at(t.pos).NewArray(elemtype, dims, elems), t)
  }

  override def visitNewClass(node: NewClassTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCNewClass]
    val encl = copy(t.encl, p)
    val typeargs = copy(t.typeargs, p)
    val clazz = copy(t.clazz, p)
    val args = copy(t.args, p)
    val `def` = copy(t.`def`, p)
    setOrigin(M.at(t.pos).NewClass(encl, typeargs, clazz, args, `def`), t)
  }

  override def visitLambdaExpression(node: LambdaExpressionTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCLambda]
    val params = this.copy(t.params, p)
    val body = this.copy(t.body, p)
    setOrigin(this.M.at(t.pos).Lambda(params, body),t )
  }

  override def visitParenthesized(node: ParenthesizedTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCParens]
    val expr = copy(t.expr, p)
    setOrigin(M.at(t.pos).Parens(expr), t)
  }

  override def visitReturn(node: ReturnTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCReturn]
    val expr = copy(t.expr, p)
    setOrigin(M.at(t.pos).Return(expr), t)
  }

  override def visitMemberSelect(node: MemberSelectTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCFieldAccess]
    val selected = copy(t.selected, p)
    setOrigin(M.at(t.pos).Select(selected, t.name), t)
  }

  override def visitMemberReference(node: MemberReferenceTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCMemberReference]
    val expr = this.copy(t.expr, p)
    val typeargs = this.copy(t.typeargs, p)
    setOrigin(this.M.at(t.pos).Reference(t.mode, t.name, expr, typeargs), t)
  }

  override def visitEmptyStatement(node: EmptyStatementTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCSkip]
    setOrigin(M.at(t.pos).Skip(), t)
  }

  override def visitSwitch(node: SwitchTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCSwitch]
    val selector = copy(t.selector, p)
    val cases = copy(t.cases, p)
    setOrigin(M.at(t.pos).Switch(selector, cases), t)
  }

  override def visitSynchronized(node: SynchronizedTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCSynchronized]
    val lock = copy(t.lock, p)
    val body = copy(t.body, p)
    setOrigin(M.at(t.pos).Synchronized(lock, body), t)
  }

  override def visitThrow(node: ThrowTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCThrow]
    val expr = copy(t.expr, p)
    setOrigin(M.at(t.pos).Throw(expr), t)
  }

  override def visitCompilationUnit(node: CompilationUnitTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCCompilationUnit]
    val packageAnnotations = copy(t.packageAnnotations, p)
    val pid = copy(t.pid, p)
    val defs = copy(t.defs, p)
    setOrigin(M.at(t.pos).TopLevel(packageAnnotations, pid, defs), t)
  }

  override def visitTry(node: TryTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCTry]
    val resources = copy(t.resources, p)
    val body = copy(t.body, p)
    val catchers = copy(t.catchers, p)
    val finalizer = copy(t.finalizer, p)
    setOrigin(M.at(t.pos).Try(resources, body, catchers, finalizer), t)
  }

  override def visitParameterizedType(node: ParameterizedTypeTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCTypeApply]
    val clazz = copy(t.clazz, p)
    val arguments = copy(t.arguments, p)
    setOrigin(M.at(t.pos).TypeApply(clazz, arguments), t)
  }

  override def visitUnionType(node: UnionTypeTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCTypeUnion]
    val components = copy(t.alternatives, p)
    setOrigin(M.at(t.pos).TypeUnion(components), t)
  }

  override def visitIntersectionType(node: IntersectionTypeTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCTypeIntersection]
    val bounds = this.copy(t.bounds, p)
    setOrigin(this.M.at(t.pos).TypeIntersection(bounds), t)
  }

  override def visitArrayType(node: ArrayTypeTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCArrayTypeTree]
    val elemtype = copy(t.elemtype, p)
    setOrigin(M.at(t.pos).TypeArray(elemtype), t)
  }

  override def visitTypeCast(node: TypeCastTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCTypeCast]
    val clazz = copy(t.clazz, p)
    val expr = copy(t.expr, p)
    setOrigin(M.at(t.pos).TypeCast(clazz, expr), t)
  }

  override def visitPrimitiveType(node: PrimitiveTypeTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCPrimitiveTypeTree]
    setOrigin(M.at(t.pos).TypeIdent(t.typetag), t)
  }

  override def visitTypeParameter(node: TypeParameterTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCTypeParameter]
    val bounds = copy(t.bounds, p)
    setOrigin(M.at(t.pos).TypeParameter(t.name, bounds), t)
  }

  override def visitInstanceOf(node: InstanceOfTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCInstanceOf]
    val expr = copy(t.expr, p)
    val clazz = copy(t.clazz, p)
    setOrigin(M.at(t.pos).TypeTest(expr, clazz), t)
  }

  override def visitUnary(node: UnaryTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCUnary]
    val arg = copy(t.arg, p)
    setOrigin(M.at(t.pos).Unary(t.getTag, arg), t)
  }

  override def visitVariable(node: VariableTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCVariableDecl]
    val mods = copy(t.mods, p)
    val vartype = copy(t.vartype, p)
    val init = copy(t.init, p)
    setOrigin(M.at(t.pos).VarDef(mods, t.name, vartype, init), t)
  }

  override def visitWhileLoop(node: WhileLoopTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCWhileLoop]
    val body = copy(t.body, p)
    val cond = copy(t.cond, p)
    setOrigin(M.at(t.pos).WhileLoop(cond, body), t)
  }

  override def visitWildcard(node: WildcardTree, p: P): JCTree = {
    val t = node.asInstanceOf[JCWildcard]
    val kind = setOrigin(M.at(t.kind.pos).TypeBoundKind(t.kind.kind), t)
    val inner = copy(t.inner, p)
    setOrigin(M.at(t.pos).Wildcard(kind, inner), t)
  }

  override def visitOther(node: com.sun.source.tree.Tree, p: P): JCTree = {
    val tree = node.asInstanceOf[JCTree]
    tree.getTag.ordinal() match {
      case 1 => {
        val t = node.asInstanceOf[LetExpr]
        val defs = copy(t.defs, p)
        val expr = copy(t.expr, p)
        setOrigin(M.at(t.pos).LetExpr(defs, expr), t)
      }
      case _ => throw new AssertionError("unknown tree tag: " + tree.getTag)
    }
  }
}
