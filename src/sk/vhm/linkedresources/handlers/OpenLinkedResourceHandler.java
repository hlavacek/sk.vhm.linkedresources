package sk.vhm.linkedresources.handlers;

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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import sk.vhm.linkedresources.Activator;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenLinkedResourceHandler extends AbstractHandler {
	
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

		if (selectedLink == null) {
			MessageDialog.openInformation(window.getShell(), "Select Link", "Please select the link to open.");
			return null;
		}

		try {
			URI linkUri = new URI(null, selectedLink, null);

			URI resolvedUri = currentProject.getPathVariableManager().resolveURI(linkUri);
			URI workspaceRelativeUri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().relativize(resolvedUri);
			
			IPath resolvedPath = new Path(workspaceRelativeUri.getPath());

			
			IFile sampleFile = ResourcesPlugin.getWorkspace().getRoot().getFile(resolvedPath);
			
			if (!sampleFile.exists()) {
				MessageDialog.openInformation(window.getShell(), "Invalid Link", "Unable to find the linked file.");
				return null;
			}
			IWorkbenchPage page = window.getActivePage();
			IDE.openEditor(page, sampleFile, true);

		} catch (PartInitException e) {
			Activator.getDefault().log("Failed to open editor for file " + selectedLink, e);
		} catch (URISyntaxException e) {
			Activator.getDefault().log("Invalid file URI " + selectedLink, e);
		}
		
		return null;
	}
	
	private String getSelectedLink(IEditorPart editorPart) {
		// get editor
		if (editorPart instanceof AbstractTextEditor) {

			IEditorSite iEditorSite = editorPart.getEditorSite();
			if (iEditorSite != null) {
				// get selection provider
				ISelectionProvider selectionProvider = iEditorSite.getSelectionProvider();
				if (selectionProvider != null) {
					ISelection iSelection = selectionProvider.getSelection();

					if (!iSelection.isEmpty()) {
						return ((ITextSelection) iSelection).getText();

					}
				}
			}

		}

		return null;
	}

}
