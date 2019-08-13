package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class StructureCommand implements VcCommand {

  private final String cursorMovement;
  private final String startNavType;
  private final String[] classes;

  public StructureCommand(String cursorMovement, String[] classes) {
    // psi start/end/range containing/previous/next Psi Classes...
    this.cursorMovement = cursorMovement;
    this.startNavType = classes[0];
    this.classes = Arrays.copyOfRange(classes, 1, classes.length);
  }

  @Override
  public String run() {
    System.out.println(
        "psi " + this.cursorMovement + " " + this.startNavType + " " + String
            .join(" ", this.classes));
    try {
      ApplicationManager.getApplication().invokeAndWait(() -> {
        final Editor e = VcCommand.getEditor();
        final PsiFile psiFile = VcCommand.getPsiFile();
        final int startingOffset = e.getCaretModel().getOffset();
        PsiElement currentElement = PsiUtilBase
            .getElementAtOffset(psiFile, startingOffset);
        printHierarchy(currentElement);
        currentElement = parentElementOfNavType(currentElement, startNavType);
        System.out.println("Containing element? " + (currentElement != null));

        if (currentElement == null) {
          return;
        }

        for (String clazz : this.classes) {
          currentElement = childElementOfNavType(currentElement, clazz, startingOffset);
          System.out.println("Next element(" + clazz + ") found? " + (currentElement != null));
          if (currentElement == null) {
            return;
          }
        }
        // CurrentElement is where we want to go

        final TextRange result = currentElement.getTextRange();
        if (cursorMovement.equals("start")) {
          e.getCaretModel().moveToOffset(result.getStartOffset());
        } else if (cursorMovement.equals("end")) {
          e.getCaretModel().moveToOffset(result.getEndOffset());
        } else {
          e.getCaretModel().moveToOffset(result.getEndOffset());
          e.getSelectionModel().setSelection(result.getStartOffset(), result.getEndOffset());
        }
        e.getScrollingModel().scrollToCaret(ScrollType.CENTER);
        IdeFocusManager.getGlobalInstance().requestFocus(e.getContentComponent(), true);

      });
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return "OK";
  }

  private void printHierarchy(PsiElement element) {
    while (element != null && element.getNode() != null) {
      System.out.println(element.toString() + " " + element.getClass().getName() + " " + element
          .getNavigationElement().toString() + " " + element.getNode().getElementType().toString());

      element = element.getParent();
    }
  }

  private PsiElement parentElementOfNavType(PsiElement element, String specifier) {
    final String navType = specifier.split("#")[0];
    while (element != null && element.getNode() != null) {
//      System.out.println(element.toString() + " " + element.getClass().getName() + " " + element
//          .getNavigationElement().toString() + " " + element.getNode().getElementType().toString());
      if (matches(navType, element.getNode().getElementType().toString())) {
        return element;
      }
      element = element.getParent();
    }
    return null;
  }

  private PsiElement childElementOfNavType(PsiElement element, String specifier, int offset) {
    Queue<PsiElement> toSearch = new ArrayDeque<>();
    final String[] split = specifier.split("#");
    final String navType;
    String direction = null;
    int index;
    if (split.length == 2) {
      navType = split[0];
      try {
        index = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        direction = split[1];
        index = 0;
      }
    } else {
      navType = specifier;
      index = 0;
    }
    List<PsiElement> results = new ArrayList<>();
    Set<PsiElement> seen = new HashSet<>();
    toSearch.add(element);
    while (toSearch.peek() != null) {
      final PsiElement current = toSearch.remove();
      if (current.getNode() != null && matches(navType,
          current.getNode().getElementType().toString())) {
        results.add(current);
        continue;
      }
      PsiElement child = current.getFirstChild();
      while (child != null) {
        if (!seen.contains(child)) {
          seen.add(child);
          toSearch.add(child);
        }
        child = child.getNextSibling();
      }
    }
    if (results.size() == 0) {
      return null;
    }
    System.out.println("Results " + results.toString());
    if (direction == null) {
      if (index < 0) {
//      System.out.println("Negative index bump " + index);
        index += results.size();
//      System.out.println("Negative index bump " + index);
      }
      return results.get(index);
    } else if (direction.equals("next")) {
      results.sort(Comparator.comparingInt(PsiElement::getTextOffset));
      for (PsiElement result : results) {
        System.out
            .println("Result " + result + " offset: " + result.getTextOffset() + " > " + offset);
        if (result.getTextOffset() > offset) {
          return result;
        }
      }
      return null;
    } else if (direction.equals("last")) {
      results.sort((o1, o2) -> -Integer.compare(o1.getTextOffset(), o2.getTextOffset()));
      for (PsiElement result : results) {
        System.out.println(
            "Result " + result + " offset: " + result.getTextRange().getEndOffset() + " < "
                + offset);
        if (result.getTextRange().getEndOffset() < offset) {
          return result;
        }
      }
      return null;
    } else if (direction.equals("this")) {
      for (PsiElement result : results) {
        if (result.getTextRange().contains(offset)) {
          return result;
        }
      }
      return null;
    }
    return null;
  }

  private boolean matches(String navType, String type) {
    final String[] matchingTypes = navType.split("\\|");
    for (String matchingType : matchingTypes) {
//    System.out.println("type "+ type +" matches " + matchingType + "? " + type.matches(matchingType));
      if (!matchingType.startsWith("^")) {
        matchingType = ".*"+ matchingType;
      }
      if (type.matches(matchingType)) {
        return true;
      }
    }
    return false;
  }
}
