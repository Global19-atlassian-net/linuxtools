/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;

import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.messages.Container;

public class DockerContainer implements IDockerContainer, IAdaptable {

	private IDockerConnection parent;
	private String id;
	private List<String> names;
	private String image;
	private String command;
	private Long created;
	private String status;
	private List<IDockerPortMapping> ports;
	private Long sizeRw;
	private Long sizeRootFs;
	private IDockerContainerInfo containerInfo;

	/**
	 * Constructor.
	 * 
	 * @param connection
	 *            the Docker connection
	 * @param container
	 *            the underlying {@link Container} data returned by the
	 *            {@link DockerClient}
	 */
	public DockerContainer(final IDockerConnection connection,
			final Container container) {
		this.parent = connection;
		this.id = container.id();
		this.image = container.image();
		this.command = container.command();
		this.created = container.created();
		this.status = container.status();
		this.names = new ArrayList<>();
		if (container.names() != null) {
			for (String name : container.names()) {
				if (name.startsWith("/")) {
					this.names.add(name.substring(1));
				} else {
					this.names.add(name);
				}
			}
		}
		this.sizeRw = container.sizeRw();
		this.sizeRootFs = container.sizeRootFs();
		this.ports = new ArrayList<>();
		if (container.ports() != null) {
			for (Container.PortMapping port : container.ports()) {
				final DockerPortMapping portMapping = new DockerPortMapping(this,
						port.privatePort(), port.publicPort(), port.type(),
						port.ip());
				ports.add(portMapping);
			}
		}
		// TODO: include volumes
	}

	/**
	 * Constructor.
	 * 
	 * @param connection
	 *            the Docker connection
	 * @param container
	 *            the underlying {@link Container} data returned by the
	 *            {@link DockerClient}
	 * @param containerInfo
	 *            the {@link IDockerContainerInfo} that was previously retrieved
	 *            for this {@link IDockerContainer}, assuming it did not change
	 *            in the mean time.
	 */
	public DockerContainer(final IDockerConnection connection,
			final Container container,
			final IDockerContainerInfo containerInfo) {
		this(connection, container);
		this.containerInfo = containerInfo;
	}

	@Override
	public IDockerConnection getConnection() {
		return parent;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String image() {
		return image;
	}

	@Override
	public String command() {
		return command;
	}

	@Override
	public Long created() {
		return created;
	}

	@Override
	public String status() {
		return status;
	}

	@Override
	public Long sizeRw() {
		return sizeRw;
	}

	@Override
	public Long sizeRootFs() {
		return sizeRootFs;
	}

	@Override
	public List<IDockerPortMapping> ports() {
		return ports;
	}

	@Override
	public String name() {
		if (names != null && names.size() > 0)
			return names.get(0);
		return ""; //$NON-NLS-1$
	}

	@Override
	public List<String> names() {
		return names;
	}

	
	@Override
	public IDockerContainerInfo info() {
		return info(false);
	}

	/**
	 * @param force
	 *            <code>true</code> to force refresh, <code>false</code> to use
	 *            existing {@link IDockerContainerInfo} if it was loaded before.
	 * @return the {@link IDockerContainerInfo} by calling the Docker daemon
	 *         using the {@link IDockerConnection} associated with this
	 *         {@link IDockerContainer}.
	 */
	// TODO: add this method in the public interface
	public IDockerContainerInfo info(final boolean force) {
		if (force || isInfoLoaded()) {
			this.containerInfo = this.parent.getContainerInfo(id);
		}
		return this.containerInfo;
	}

	/**
	 * @return <code>true</code> if the {@link IDockerContainerInfo} has been
	 *         loaded, <code>false</code> otherwise.
	 */
	// TODO: add this method in the public interface
	public boolean isInfoLoaded() {
		return this.containerInfo != null;
	}

	@Override
	public String toString() {
		return "Container: id=" + id() + "\n" + "  image=" + image() + "\n"
				+ "  created=" + created() + "\n" + "  command=" + command()
				+ "\n" + "  status=<" + status() + ">\n" + "  name="
				+ name() + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerContainer other = (DockerContainer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IDockerConnection.class))
			return (T) this.parent;
		return null;
	}
	
	
}
