package org.intellij.markdown.parser.markerblocks.impl

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl

public class ParagraphMarkerBlock(constraints: MarkdownConstraints,
                                  marker: ProductionHolder.Marker,
                                  val interruptsParagraph: (LookaheadText.Position, MarkdownConstraints) -> Boolean)
        : MarkerBlockImpl(constraints, marker) {
    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int? {
        return pos.offset + 1
    }

    override fun doProcessToken(pos: LookaheadText.Position,
                                currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {

        if (pos.char != '\n') {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        assert(pos.char == '\n')

        if (MarkdownParserUtil.calcNumberOfConsequentEols(pos, constraints) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val nextLineConstraints = MarkdownConstraints.fromBase(pos, constraints)
        if (!nextLineConstraints.upstreamWith(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val posToCheck = pos.nextPosition(1 + nextLineConstraints.getIndentAdapted(pos.currentLine))
        if (posToCheck == null || interruptsParagraph(posToCheck, nextLineConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.PARAGRAPH
    }

}
