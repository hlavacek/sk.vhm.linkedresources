package sk.vhm.linkedresources.popup.actions;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import sk.vhm.linkedresources.Activator;

public class OpenLinkedResourceAction implements IObjectActionDelegate {

	private Shell shell;

	/**
	 * Constructor for Action1.
	 */
	public OpenLinkedResourceAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {

		IEditorPart editorPart = getActiveEditor();

		IFile activeFile = (IFile) editorPart.getEditorInput().getAdapter(IFile.class);
		IProject currentProject = activeFile.getProject();

		if (currentProject == null) {
			MessageDialog.openInformation(shell, "No current project", "The file does not have a project associated.");
			return;
		}

		String selectedLink = getSelectedLink(editorPart);

		if (selectedLink == null) {
			MessageDialog.openInformation(shell, "Select Link", "Please select the link to open.");
			return;
		}

		try {
			URI linkUri = new URI(null, selectedLink, null);

			URI resolvedUri = currentProject.getPathVariableManager().resolveURI(linkUri);
			URI workspaceRelativeUri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().relativize(resolvedUri);
			
			IPath resolvedPath = new Path(workspaceRelativeUri.getPath());

			
			IFile sampleFile = ResourcesPlugin.getWorkspace().getRoot().getFile(resolvedPath);
			
			if (!sampleFile.exists()) {
				MessageDialog.openInformation(shell, "Invalid Link", "Unable to find the linked file.");
				return;
			}
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IDE.openEditor(page, sampleFile, true);

		} catch (PartInitException e) {
			Activator.getDefault().log("Failed to open editor for file " + selectedLink, e);
		} catch (URISyntaxException e) {
			Activator.getDefault().log("Invalid file URI " + selectedLink, e);
		}

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

	private IEditorPart getActiveEditor() {
		return Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

}
