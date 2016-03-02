package de.danielbechler.diff.differ;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.PrintingVisitor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mgalyutin on 07.02.16.
 */
public class CollectionIssuesTest {

    public static class Person {

        String name;

        List<Location> locations = new ArrayList<Location>();

        public Person withName(String name)  {
            this.name = name;

            return this;
        }

        public Person withLocation(Location location) {
            locations.add(location);

            return this;
        }

        public List<Location> getLocations() {
            return locations;
        }

        public Person setLocations(List<Location> locations) {
            this.locations = locations;
            return this;
        }

        public String getName() {
            return name;
        }

        public Person setName(String name) {
            this.name = name;
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

    public static class Location {

        String address;

        Location withAddress(String address)  {
            this.address = address;
            return this;
        }

        public String getAddress() {
            return address;
        }

        public Location setAddress(String address) {
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
    public void addLocationPropertyWithArrays() throws Exception {
        Object working = Arrays.asList(new Person().withName("a")
                .withLocation(new Location().withAddress("address a"))
                .withLocation(new Location().withAddress("address b"))
        );

        Object base = Arrays.asList(new Person().withName("a")
                .withLocation(new Location().withAddress("address a")));

        assertEquals("Property at path '/[Person{name='a', locations=[Location{address='address a'}]}]/locations[Location{address='address b'}]/address' has been added => [ address b ]\n", compareObjects(working, base));
    }

    @Test
    public void addLocationProperty() throws Exception {
        Object working = new Person().withName("a")
                .withLocation(new Location().withAddress("address a"))
                .withLocation(new Location().withAddress("address b"));

        Object base = new Person().withName("a")
                .withLocation(new Location().withAddress("address a"));

        assertEquals("Property at path '/locations[Location{address='address b'}]/address' has been added => [ address b ]\n", compareObjects(working, base));
    }


    @Test
    public void compareLocations() throws Exception {
        // default differ object relays on equals method implementation to identify instances
        // it can be overridden by configuring identity service

        Object working = new Location().withAddress("address a");
        Object base = new Location().withAddress("address b");

        DiffNode node = ObjectDifferBuilder.buildDefault()
                .compare(working, base);


        assertEquals("Property at path '/address' has changed from [ address b ] to [ address a ]\n", difference(working, base, node));
    }


    @Test
    public void differenceWithSubCollections() throws Exception {
        Object working = Arrays.asList(Arrays.asList("a", "b", "c"));
        Object base = Arrays.asList(Arrays.asList("a", "b"));

        assertEquals("Property at path '/[[a, b]]' has been added => [ c ]\n", compareObjects(working, base));
    }

    @Test
    public void illustratePrimitiveCollectionDiff() throws Exception {
        List<String> working = Arrays.asList("a", "b", "c");
        List<String> base = Arrays.asList("a", "b");

        assertEquals("Property at path '/[c]' has been added => [ c ]\n", compareObjects(working, base));
    }

    public static String compareObjects(Object working, Object base) {
        DiffNode node = ObjectDifferBuilder.buildDefault().compare(working, base);

        return difference(working, base, node);
    }

    public static String difference(Object working, Object base, DiffNode node) {
        TestablePrintingVisitor visitor = new TestablePrintingVisitor(working, base);
        node.visit(visitor);

        return visitor.getOutput();
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
