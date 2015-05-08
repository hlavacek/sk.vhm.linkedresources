package sk.vhm.linkedresources.hyperlink;

import static java.util.Objects.requireNonNull;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

import sk.vhm.linkedresources.Activator;

public class EditPointedResourceLinkDetector extends AbstractEditLinkedFileLinkDetector {

	@Override
	protected IRegion getHyperlinkRegion(ITextViewer textViewer, IRegion region) {
		IDocument document = requireNonNull(textViewer.getDocument(), "Failed to get document for editor.");

		int endOffset = getOffset(document, Activator.LINK_DELIMITERS, region.getOffset(), 1);
		int startOffset = getOffset(document, Activator.LINK_DELIMITERS, region.getOffset(), -1) + 1;

		return new Region(startOffset, endOffset - startOffset);
	}

	private int getOffset(IDocument document, String linkDelimiters, int startOffset, int increment) {

		int documentLenght = document.getLength();
		int currentOffset = startOffset;
		try {
			while (currentOffset >= 0 && currentOffset < documentLenght) {
				char currentChar = document.getChar(currentOffset);

				if (linkDelimiters.indexOf(currentChar) > -1) {
					return currentOffset;
				}

				currentOffset += increment;
			}

		} catch (BadLocationException e) {
			Activator.getDefault().log("Failed to get character from document: " + currentOffset, e);
		}

		return currentOffset;
	}

}
