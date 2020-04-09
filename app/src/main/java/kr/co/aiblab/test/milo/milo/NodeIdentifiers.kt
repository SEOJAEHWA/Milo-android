package kr.co.aiblab.test.milo.milo

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned

object NodeIdentifiers {

    val PositionX: NodeId = init(2, 2070)
    val PositionY: NodeId = init(2, 2071)
    val PositionZ: NodeId = init(2, 2072)

    private fun init(ns: Int, value: Int): NodeId =
        NodeId(Unsigned.ushort(ns), Unsigned.uint(value))

}