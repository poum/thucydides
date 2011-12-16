package net.thucydides.core.pages.components;

import ch.lambdaj.function.convert.Converter;
import net.thucydides.core.matchers.BeanMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.convert;

/**
 * Class designed to make it easier reading from and reasoning about data in HTML tables.
 */
public class HtmlTable {
    private final WebElement tableElement;

    public HtmlTable(final WebElement tableElement) {
        this.tableElement = tableElement;
    }

    public List<Map<String, String>> getRows() {

        List<Map<String, String>> results = new ArrayList<Map<String, String>>();

        List<String> headings = getHeadings();
        List<WebElement> rows = getRowElements();

        for (WebElement row : rows) {
            List<WebElement> cells = cellsIn(row);
            if (enoughCellsFor(headings).in(cells)) {
                results.add(rowDataFrom(cells, headings));
            }
        }
        return results;
    }
    
    private class EnoughCellsCheck {
        private final int minimumNumberOfCells;

        private EnoughCellsCheck(List<String> headings) {
            this.minimumNumberOfCells = headings.size();
        }
        
        public boolean in(List<WebElement> cells) {
            return (cells.size() >= minimumNumberOfCells);
        }
    }

    private EnoughCellsCheck enoughCellsFor(List<String> headings) {
        return new EnoughCellsCheck(headings);
    }

    private boolean enoughCellsAreIn(List<WebElement> cells , int minimumCellCount) {
        return (cells.size() >= minimumCellCount);
    }

    public List<String> getHeadings() {
        return convert(getHeadingElements(), toTextValues());
    }

    public List<WebElement> getHeadingElements() {
        return tableElement.findElements(By.tagName("th"));
    }

    public List<WebElement> getRowElements() {
        return tableElement.findElements(By.xpath(".//tr[td]"));
    }

    public List<WebElement> getRowElementsMatching(BeanMatcher... matchers) {

        List<WebElement> rowElements = getRowElements();
        List<Integer> matchingRowIndexes = findMatchingIndexesFor(rowElements, matchers);

        List<WebElement> matchingElements = new ArrayList<WebElement>();
        for(Integer index : matchingRowIndexes) {
            matchingElements.add(rowElements.get(index));
        }
        return matchingElements;
    }

    private List<Integer> findMatchingIndexesFor(List<WebElement> rowElements,
                                                 BeanMatcher[] matchers) {
        List<Integer> indexes = new ArrayList<Integer>();
        List<String> headings = getHeadings();

        int index = 0;
        for(WebElement row : rowElements) {
            List<WebElement> cells = cellsIn(row);
            Map<String, String> rowData = rowDataFrom(cells, headings);
            if (matches(rowData, matchers)) {
                indexes.add(index);
            }
            index++;
        }

        return indexes;
    }

    private boolean matches(Map<String, String> rowData, BeanMatcher[] matchers) {
        for(BeanMatcher matcher : matchers) {
            if (!matcher.matches(rowData)) {
                return false;
            }
        }
        return true;
    }


    private Map<String,String> rowDataFrom(List<WebElement> cells, List<String> headings) {
        Map<String,String> rowData = new HashMap<String, String>();

        int column = 0;
        for (String heading : headings) {
            String cell = cellValueAt(column++, cells);
            if (cell != null) {
                rowData.put(heading, cell);
            }
        }
        return rowData;
    }

    private List<WebElement> cellsIn(WebElement row) {
        return row.findElements(By.tagName("td"));
    }

    private String cellValueAt(final int column, final List<WebElement> cells) {
        if (column < cells.size()) {
            return cells.get(column).getText();
        } else {
            return null;
        }
    }

    private Converter<WebElement, String> toTextValues() {
        return new Converter<WebElement, String>() {
            @Override
            public String convert(WebElement from) {
                return from.getText();
            }
        };
    }

    public static List<Map<String, String>> rowsFrom(final WebElement table) {
        return new HtmlTable(table).getRows();
    }

    public static List<WebElement> filterRows(final WebElement table, final BeanMatcher... matchers) {
        return new HtmlTable(table).getRowElementsMatching(matchers);
    }
}