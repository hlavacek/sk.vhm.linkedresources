package sk.vhm.linkedresources.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import sk.vhm.linkedresources.Activator;

public class LinkedResourceHyperlink implements IHyperlink {

	private IRegion urlRegion;
	private String linkText;
	private IFile linkedFile;

	public LinkedResourceHyperlink(IRegion urlRegion, String linkText, IFile linkedFile) {
		this.urlRegion = urlRegion;
		this.linkText = linkText;
		this.linkedFile = linkedFile;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return urlRegion;
	}

	@Override
	public String getTypeLabel() {
		return linkText;
	}

	@Override
	public String getHyperlinkText() {
		return linkText;
	}

	@Override
	public void open() {
		try {
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), linkedFile, true);
		} catch (PartInitException e) {
			Activator.getDefault().log("Failed to open editor for file " + linkText, e);
		}

	}

}
