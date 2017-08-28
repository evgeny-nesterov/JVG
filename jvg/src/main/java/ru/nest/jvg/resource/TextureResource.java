package ru.nest.jvg.resource;

public class TextureResource extends Resource<Texture> {
	private Texture texture;

	public TextureResource(Texture texture) {
		setResource(texture);
	}

	@Override
	public Texture getResource() {
		return texture;
	}

	@Override
	public void setResource(Texture texture) {
		this.texture = texture;
	}
}
