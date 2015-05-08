package sk.vhm.linkedresources.hyperlink;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public abstract class AbstractEditLinkedFileLinkDetector extends AbstractLinkedResourceDetector {

	protected IHyperlink[] getHyperlink(IRegion targetRegion, String linkText, IResource linkedFile) {
		return new IHyperlink[] { new OpenEditorHyperlink(targetRegion, linkText, (IFile) linkedFile) };
	}

	protected IResource getLinkedFile(String selectedLink) {
	
		IPath resolvedPath;
		try {
			resolvedPath = resolvePath(selectedLink);
			
			return ResourcesPlugin.getWorkspace().getRoot().getFile(resolvedPath);
		} catch (URISyntaxException | IllegalArgumentException e) {
			return null;
		}
	
	}

	protected IPath resolvePath(String selectedLink) throws URISyntaxException {
	
		URI linkUri = new URI(null, selectedLink, null);
	
		URI resolvedUri = getCurrentProject().getPathVariableManager().resolveURI(linkUri);
		URI workspaceRelativeUri = ResourcesPlugin.getWorkspace().getRoot().getLocationURI().relativize(resolvedUri);
	
		IPath resolvedPath = new Path(workspaceRelativeUri.getPath());
		return resolvedPath;
	}

}