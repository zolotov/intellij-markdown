package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

class ImageParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.EXCLAMATION_MARK
                    && iterator.rawLookup(1) == MarkdownTokenTypes.LBRACKET) {
                val link = InlineLinkParser.parseInlineLink(iterator.advance())
                        ?: ReferenceLinkParser.parseReferenceLink(iterator.advance())

                if (link != null) {
                    result = result
                            .withNode(SequentialParser.Node(iterator.index..link.iteratorPosition.index + 1, MarkdownElementTypes.IMAGE))
                            .withOtherParsingResult(link)
                    iterator = link.iteratorPosition.advance()
                    continue
                }
            }

            delegateIndices.add(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))

    }
}