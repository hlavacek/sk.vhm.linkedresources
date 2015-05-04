package sk.vhm.linkedresources.handlers;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import sk.vhm.linkedresources.Activator;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenLinkedResourceHandler extends AbstractHandler {

	private static final String LINK_DELIMITERS = " :\n\t";

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IEditorPart editorPart = Activator.getActiveEditor();

		IFile activeFile = (IFile) editorPart.getEditorInput().getAdapter(IFile.class);
		IProject currentProject = activeFile.getProject();

		if (currentProject == null) {
			MessageDialog.openInformation(window.getShell(), "No current project", "The file does not have a project associated.");
			return null;
		}

		String selectedLink = getSelectedLink(editorPart);

		if (selectedLink == null || "".equals(selectedLink)) {
			MessageDialog.openInformation(window.getShell(), "Select Link", "Please select the link to open.");
			return null;
		}

		try {
			IFile linkedFile = getLinkedFile(currentProject, selectedLink);

			if (!linkedFile.exists()) {
				MessageDialog.openInformation(window.getShell(), "Invalid Link", "Unable to find the linked file.");
				return null;
			}
			openEditor(window, linkedFile);

		} catch (PartInitException e) {
			Activator.getDefault().log("Failed to open editor for file " + selectedLink, e);
		} catch (URISyntaxException e) {
			Activator.getDefault().log("Invalid file URI " + selectedLink, e);
		}

		return null;
	}

	private void openEditor(IWorkbenchWindow window, IFile linkedFile) throws PartInitException {
		IWorkbenchPage page = window.getActivePage();
		IDE.openEditor(page, linkedFile, true);
	}

	private IFile getLinkedFile(IProject currentProject, String selectedLink) throws URISyntaxException {
		URI linkUri = new URI(null, selectedLink, null);

		URI resolvedUri = currentProject.getPathVariableManager().resolveURI(linkUri);
		URI workspaceRelativeUri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().relativize(resolvedUri);

		IPath resolvedPath = new Path(workspaceRelativeUri.getPath());

		IFile sampleFile = ResourcesPlugin.getWorkspace().getRoot().getFile(resolvedPath);
		return sampleFile;
	}

	private String getSelectedLink(IEditorPart editorPart) {
		// get editor
		if (!(editorPart instanceof AbstractTextEditor)) {
			Activator.getDefault().log("The current editor is not a text editor, cannot get selected link.");
			return null;
		}

		ITextEditor textEditor = (ITextEditor) editorPart;

		IDocumentProvider provider = requireNonNull(textEditor.getDocumentProvider(), "Failed to get document provider.");

		IEditorSite iEditorSite = requireNonNull(editorPart.getEditorSite(), "Failed to get editor site.");

		ISelectionProvider selectionProvider = requireNonNull(iEditorSite.getSelectionProvider(), "Failed to get selection provider for editor.");

		ITextSelection iSelection = (ITextSelection) selectionProvider.getSelection();
		
		String selectedText = iSelection.getText();

		if (!iSelection.isEmpty() && !"".equals(selectedText)) {
			Activator.getDefault().log("Using selected text for a link: " + selectedText);
			return selectedText;
		}

		Activator.getDefault().log("There is no selected text, trying to find a link.");
		IDocument document = requireNonNull(provider.getDocument(editorPart.getEditorInput()), "Failed to get document for editor.");

		int endOffset = getOffet(document, LINK_DELIMITERS, iSelection.getOffset(), 1);
		int startOffset = getOffet(document, LINK_DELIMITERS, iSelection.getOffset(), -1) + 1;
		
		try {
			return document.get(startOffset, endOffset - startOffset);
		} catch (BadLocationException e) {
			Activator.getDefault().log("Failed to get text from document: " + startOffset + "," + endOffset, e);
		} 
		// int offset = textSelection.getOffset();
		// int lineNumber = document.getLineOfOffset(offset);

		return null;
	}

	private int getOffet(IDocument document, String linkDelimiters, int startOffset, int increment) {

		int documentLenght = document.getLength();
		int currentOffset = startOffset;
		try {
			while (currentOffset >= 0 && currentOffset < documentLenght) {
				char currentChar = document.getChar(currentOffset);

				if (LINK_DELIMITERS.indexOf(currentChar) > -1) {
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
