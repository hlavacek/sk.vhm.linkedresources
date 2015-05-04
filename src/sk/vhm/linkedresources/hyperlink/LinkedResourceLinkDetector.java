package sk.vhm.linkedresources.hyperlink;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import sk.vhm.linkedresources.Activator;

public class LinkedResourceLinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		int offset = region.getOffset();

		IRegion targetRegion = getSelectedLink(textViewer);

		if (targetRegion == null) {
			targetRegion = detectLink(textViewer, offset);
		}
		if (targetRegion == null) {
			return null;
		}

		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFile activeFile = (IFile) activeEditor.getEditorInput().getAdapter(IFile.class);
		IProject currentProject = activeFile.getProject();
		IDocument document = textViewer.getDocument();

		try {

			String linkText = document.get(targetRegion.getOffset(), targetRegion.getLength());

			IFile linkedFile = getLinkedFile(currentProject, linkText);

			if (linkedFile.exists()) {

				return new IHyperlink[] { new LinkedResourceHyperlink(targetRegion, linkText, linkedFile) };
			}

		} catch (BadLocationException | URISyntaxException | IllegalArgumentException e) {
			Activator.getDefault().log("Failed to get text from document: " + targetRegion.getOffset() + "," + targetRegion.getLength(), e);
		}

		return null;

	}

	private Region getSelectedLink(ITextViewer textViewer) {

		ISelectionProvider selectionProvider = requireNonNull(textViewer.getSelectionProvider(), "Failed to get selection provider for editor.");

		ITextSelection iSelection = (ITextSelection) selectionProvider.getSelection();

		String selectedText = iSelection.getText();

		if (!iSelection.isEmpty() && !"".equals(selectedText)) {
			Activator.getDefault().log("Using selected text for a link: " + selectedText);
			return new Region(iSelection.getOffset(), iSelection.getLength());
		}
		return null;

	}

	private Region detectLink(ITextViewer textViewer, int currentOffset) {
		IDocument document = requireNonNull(textViewer.getDocument(), "Failed to get document for editor.");

		int endOffset = getOffset(document, Activator.LINK_DELIMITERS, currentOffset, 1);
		int startOffset = getOffset(document, Activator.LINK_DELIMITERS, currentOffset, -1) + 1;

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

	private IFile getLinkedFile(IProject currentProject, String selectedLink) throws URISyntaxException {
		URI linkUri = new URI(null, selectedLink, null);

		URI resolvedUri = currentProject.getPathVariableManager().resolveURI(linkUri);
		URI workspaceRelativeUri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().relativize(resolvedUri);

		IPath resolvedPath = new Path(workspaceRelativeUri.getPath());

		IFile sampleFile = ResourcesPlugin.getWorkspace().getRoot().getFile(resolvedPath);
		return sampleFile;
	}

}
