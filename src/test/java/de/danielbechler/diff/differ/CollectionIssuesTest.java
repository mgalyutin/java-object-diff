package de.danielbechler.diff.differ;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.PrintingVisitor;
import de.danielbechler.diff.node.ToMapPrintingVisitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mgalyutin on 07.02.16.
 */
public class CollectionIssuesTest {

    static class Person {

        String name;

        List<Location> locations = new ArrayList<Location>();

        Person withName(String name)  {
            this.name = name;

            return this;
        }

        Person withLocation(Location location) {
            locations.add(location);

            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Person person = (Person) o;

            return !(name != null ? !name.equals(person.name) : person.name != null);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", locations=" + locations +
                    '}';
        }
    }

    static class Location {

        String address;

        Location withAddress(String address)  {
            this.address = address;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            return !(address != null ? !address.equals(location.address) : location.address != null);

        }

        @Override
        public int hashCode() {
            return address != null ? address.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "address='" + address + '\'' +
                    '}';
        }
    }


    @Test
    public void realisticExample() throws Exception {
        Object working = Arrays.asList(new Person().withName("a").withLocation(new Location().withAddress("address a")));
        Object base = Arrays.asList(new Person().withName("a").withLocation(new Location().withAddress("address b").withAddress("address b")));

        assertEquals("", compareObejcts(working, base));
    }


    @Test
    public void differenceWithSubCollections() throws Exception {
        Object working = Arrays.asList(Arrays.asList("a", "b", "c"));
        Object base = Arrays.asList(Arrays.asList("a", "b"));

        assertEquals("Property at path '/[[a, b]]' has been added => [ c ]\n", compareObejcts(working, base));
    }

    @Test
    public void illustratePrimitiveCollectionDiff() throws Exception {
        List<String> working = Arrays.asList("a", "b", "c");
        List<String> base = Arrays.asList("a", "b");

        assertEquals("Property at path '/[c]' has been added => [ c ]\n", compareObejcts(working, base));
    }

    private String compareObejcts(Object working, Object base) {
        DiffNode node = ObjectDifferBuilder.buildDefault().compare(working, base);

        TestablePrintingVisitor visitor = new TestablePrintingVisitor(working, base);
        node.visit(visitor);

        return visitor.getOutput();
    }

    private static class TestablePrintingVisitor extends PrintingVisitor
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
