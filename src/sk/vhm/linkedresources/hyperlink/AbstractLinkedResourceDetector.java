package sk.vhm.linkedresources.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import sk.vhm.linkedresources.Activator;

public abstract class AbstractLinkedResourceDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IRegion targetRegion = getHyperlinkRegion(textViewer, region);

		if (targetRegion == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();

		try {

			String linkText = document.get(targetRegion.getOffset(), targetRegion.getLength());

			IResource linkedFile = getLinkedFile(linkText);

			if (linkedFile != null && linkedFile.exists()) {
				return getHyperlink(targetRegion, linkText, linkedFile);

			}

		} catch (BadLocationException e) {
			Activator.getDefault().log("Failed to get text from document: " + targetRegion.getOffset() + "," + targetRegion.getLength(), e);
		}

		return null;

	}
	
	protected IProject getCurrentProject() {
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFile activeFile = (IFile) activeEditor.getEditorInput().getAdapter(IFile.class);
		IProject currentProject = activeFile.getProject();
		return currentProject;
	}

	protected abstract IHyperlink[] getHyperlink(IRegion targetRegion, String linkText, IResource linkedFile);

	protected abstract IResource getLinkedFile(String linkText);

	protected abstract IRegion getHyperlinkRegion(ITextViewer textViewer, IRegion region);

}