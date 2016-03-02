package de.danielbechler.diff.differ;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.PrintingVisitor;
import de.danielbechler.diff.path.NodePath;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author mgalyutin
 *         Date: 18.02.16
 *         Time: 10:28
 */
public class MapIssuesTest {

	static class MapBuilder<K, V> {

		Map<K, V> map = new HashMap<K, V>();

		MapBuilder<K, V> put(K key, V value) {
			map.put(key, value);
			return this;
		}

		Map<K, V> build() {
			return map;
		}

	}

	@Test
	public void compareWithCollections() throws Exception {
		Object working = new MapBuilder<String, Object>()
				.put("name", "alice")
				.put("locations", Arrays.asList(
						new MapBuilder<String, Object>()
								.put("street", "street1")
								.put("city", "city")
								.build()))
				.build();
		Object base = new MapBuilder<String, Object>()
				.put("name", "alice")
				.put("locations", Arrays.asList(
						new MapBuilder<String, Object>()
								.put("street", "street2")
								.put("city", "city")
								.build()))
				.build();
		DiffNode node = ObjectDifferBuilder.startBuilding()
				.identity()
				.ofCollectionItems(NodePath.startBuilding()
						.mapKey("locations")
						.build()).via(new IdentityStrategy() {
					public boolean equals(Object working, Object base) {
						Object w = get(working, "city");
						Object b = get(base, "city");
						if (w != null) {
							return w.equals(b);
						} else {
							return b == null;
						}

					}

					private Object get(Object base, String name) {
						if (base instanceof Map) {
							return ((Map) base).get(name);
						}
						return null;
					}
				})
				.and()
				.build().compare(working, base);

		TestablePrintingVisitor visitor = new TestablePrintingVisitor(working, base);
		node.visit(visitor);

		assertEquals("\n", visitor.getOutput());
	}


	@Test
	public void compareSubMaps() throws Exception {
		assertEquals("Property at path '/{b}{a}' has changed from [ b ] to [ a ]\n", CollectionIssuesTest.compareObjects(
				new MapBuilder<String, Object>()
						.put("name", "alice")
						.put("location", new MapBuilder<String, Object>()
								.put("street", "street1")
								.put("city", "city")
								.build())
						.build(),
				new MapBuilder<String, Object>()
						.put("name", "alice")
						.put("location", new MapBuilder<String, Object>()
								.put("street", "street2")
								.put("city", "city")
								.build())
						.build()
				));
	}

	@Test
	public void compareSimpleMaps() throws Exception {
		assertEquals("Property at path '/{a}' has changed from [ b ] to [ a ]\n", CollectionIssuesTest.compareObjects(
				new MapBuilder<String, Object>().put("a", "a").build(),
				new MapBuilder<String, Object>().put("a", "b").build()
		));
	}

	public static class TestablePrintingVisitor extends PrintingVisitor
	{
		private final StringBuilder sb = new StringBuilder();

		public TestablePrintingVisitor(final Object working, final Object base)
		{
			super(working, base);
		}

		@Override
		protected void print(final String text)
		{
			sb.append(text).append('\n');
		}

		public String getOutput()
		{
			return sb.toString();
		}
	}

}
