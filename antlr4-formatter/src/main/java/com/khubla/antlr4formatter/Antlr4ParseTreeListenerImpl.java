/*
 * Antlr4Formatter Copyright 2015, khubla.com
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.khubla.antlr4formatter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.parser.antlr4.ANTLRv4Parser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Antlr4ParseTreeListenerImpl implements ParseTreeListener {
   /**
   *
   */
   private static final Logger LOG = LoggerFactory.getLogger(Antlr4ParseTreeListenerImpl.class);
   /**
    * non space tokens
    */
   private static final Set<String> noSpacingBeforeTokens = new HashSet<>(Arrays.asList(new String[] { "?", "*", ";", ")" }));
   private static final Set<String> noSpacingAfterTokens = new HashSet<>(Arrays.asList(new String[] { "(" }));
   /**
    * comment tokens
    */
   private static final Set<String> commentTokens = new HashSet<>(Arrays.asList(new String[] { "/*", "//" }));
   /**
    * indent
    */
   private int indent = 0;
   /**
    * current context
    */
   private ParserRuleContext ctx;
   /**
    * newline
    */
   private boolean newline = true;
   /**
    * PrintStream
    */
   private final Writer writer;
   /**
    * token stream
    */
   private final CommonTokenStream commonTokenStream;
   /**
    * left hidden token marker
    */
   private int leftHiddenTokenPos = -1;
   /**
    * right hidden token marker
    */
   private int rightHiddenTokenPos = -1;
   /**
    * parenth count
    */
   private int parenthCount = 0;
   /**
    * previous token
    */
   private String previousToken = "";

   /**
    * ctor
    */
   public Antlr4ParseTreeListenerImpl(Writer writer, CommonTokenStream commonTokenStream) {
      this.writer = writer;
      this.commonTokenStream = commonTokenStream;
   }

   /**
    * build indent
    */
   private String buildIndent(int indent) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < (indent * 3); i++) {
         sb.append(" ");
      }
      return sb.toString();
   }

   @Override
   public void enterEveryRule(ParserRuleContext ctx) {
      /*
       * left hidden tokens
       */
      printLeftHiddenTokens(ctx);
      /*
       * ctx change
       */
      if (ctx instanceof ANTLRv4Parser.RuleSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.GrammarSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.OptionsSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.TokensSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.ChannelsSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.ModeSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.LexerRuleSpecContext) {
         this.ctx = ctx;
         writeCR();
      } else if (ctx instanceof ANTLRv4Parser.LabeledAltContext) {
         this.ctx = ctx;
      } else if (ctx instanceof ANTLRv4Parser.ActionBlockContext) {
         this.ctx = ctx;
      }
   }

   @Override
   public void exitEveryRule(ParserRuleContext ctx) {
      printRightHiddenTokens(ctx);
   }

   /**
    * check if string is comment
    */
   private boolean isComment(String str) {
      for (final String c : commentTokens) {
         if (str.startsWith(c)) {
            return true;
         }
      }
      return false;
   }

   private void printLeftHiddenTokens(ParserRuleContext ctx) {
      /*
       * check for hidden tokens left of the ctx
       */
      final int tokPos = tokenStart(ctx);
      if (tokPos > leftHiddenTokenPos) {
         leftHiddenTokenPos = tokPos;
         final List<Token> refChannel = commonTokenStream.getHiddenTokensToLeft(tokPos, ANTLRv4Lexer.COMMENT);
         if ((null != refChannel) && (refChannel.size() > 0)) {
            for (final Token token : refChannel) {
               /*
                * print comments
                */
               final String str = token.getText().trim();
               if (str.length() > 0) {
                  if (isComment(str)) {
                     writeComment(ctx, token.getText());
                  }
               }
            }
         }
      }
   }

   private void printRightHiddenTokens(ParserRuleContext ctx) {
      /*
       * check for hidden tokens right of the ctx
       */
      final int tokPos = tokenStop(ctx);
      if (tokPos > rightHiddenTokenPos) {
         rightHiddenTokenPos = tokPos;
         final List<Token> refChannel = commonTokenStream.getHiddenTokensToRight(tokPos, ANTLRv4Lexer.COMMENT);
         if ((null != refChannel) && (refChannel.size() > 0)) {
            for (final Token token : refChannel) {
               /*
                * print comments
                */
               final String str = token.getText().trim();
               if (str.length() > 0) {
                  if (isComment(str)) {
                     writeComment(ctx, token.getText());
                  }
               }
            }
         }
      }
   }

   int tokenStart(ParserRuleContext ctx) {
      return (ctx.getStart().getTokenIndex() < ctx.getStop().getTokenIndex()) ? ctx.getStart().getTokenIndex() : ctx.getStop().getTokenIndex();
   }

   int tokenStop(ParserRuleContext ctx) {
      return (ctx.getStop().getTokenIndex() > ctx.getStart().getTokenIndex()) ? ctx.getStop().getTokenIndex() : ctx.getStart().getTokenIndex();
   }

   @Override
   public void visitErrorNode(ErrorNode node) {
      System.out.println("Error: " + node.getText());
   }

   @Override
   public void visitTerminal(TerminalNode node) {
      /*
       * log the rule
       */
      LOG.debug(ctx.getClass().getSimpleName() + " : " + node.getText());
      /*
       * options indenting
       */
      if ((ctx instanceof ANTLRv4Parser.OptionsSpecContext) || (ctx instanceof ANTLRv4Parser.ModeSpecContext) || (ctx instanceof ANTLRv4Parser.TokensSpecContext)
            || (ctx instanceof ANTLRv4Parser.ChannelsSpecContext)) {
         if (node.getSymbol().getType() == ANTLRv4Lexer.LBRACE) {
            indent++;
            writeCR();
            write(node);
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.RBRACE) {
            write(node);
            indent--;
            writeCR();
         } else {
            write(node);
         }
      } else if (ctx instanceof ANTLRv4Parser.LabeledAltContext) {
         if (node.getSymbol().getType() == ANTLRv4Lexer.LPAREN) {
            parenthCount++;
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.RPAREN) {
            parenthCount--;
         }
         if (node.getSymbol().getType() == ANTLRv4Lexer.SEMI) {
            writeCR();
            write(node);
            indent--;
            writeCR();
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.OR) {
            if (parenthCount == 0) {
               writeCR();
            }
            write(node);
         } else {
            if (node.getSymbol().getType() != Recognizer.EOF) {
               write(node);
            }
         }
      }
      /*
       * rule indenting
       */
      else if ((ctx instanceof ANTLRv4Parser.RuleSpecContext) || (ctx instanceof ANTLRv4Parser.LexerRuleSpecContext)) {
         parenthCount = 0;
         if (node.getSymbol().getType() == ANTLRv4Lexer.COLON) {
            indent++;
            writeCR();
            write(node);
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.SEMI) {
            writeCR();
            write(node);
            indent--;
            writeCR();
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.DOC_COMMENT) {
            write(node);
            writeCR();
         } else {
            if (node.getSymbol().getType() != Recognizer.EOF) {
               write(node);
            }
         }
      }
      /*
       * grammar spec
       */
      else if (ctx instanceof ANTLRv4Parser.GrammarSpecContext) {
         if (node.getSymbol().getType() == ANTLRv4Lexer.SEMI) {
            write(node);
            writeCR();
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.DOC_COMMENT) {
            write(node);
            writeCR();
         } else {
            write(node);
         }
      }
      /*
       * action spec
       */
      else if (ctx instanceof ANTLRv4Parser.ActionBlockContext) {
         if (node.getSymbol().getType() == ANTLRv4Lexer.SEMI) {
            write(node);
            writeCR();
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.DOC_COMMENT) {
            write(node);
            writeCR();
         } else if (node.getSymbol().getType() == ANTLRv4Lexer.AT) {
            write(node);
            writeCR();
         } else {
            write(node);
         }
      }
      /*
       * grammar spec
       */
      else {
         write(node);
      }
   }

   /**
    * write to the output stream
    */
   private void write(String str) {
      /*
       * print token
       */
      writeSimple(str);
      if (true == newline) {
         newline = false;
      }
      /*
       * save the previous
       */
      previousToken = str;
   }

   /**
    * write to the output stream
    */
   private void write(TerminalNode node) {
      LOG.debug("Writing: '" + node.getText() + "'");
      /*
       * space before the output
       */
      if (false == (ctx instanceof ANTLRv4Parser.ActionBlockContext)) {
         if ((false == newline) && (false == noSpacingAfterTokens.contains(previousToken)) && (false == noSpacingBeforeTokens.contains(node.getText()))) {
            writeSimple(" ");
         }
      }
      /*
       * print token
       */
      writeSimple(node.getText());
      if (true == newline) {
         newline = false;
      }
      /*
       * save the previous
       */
      previousToken = node.getText();
   }

   /**
    * write a comment
    */
   private void writeComment(ParserRuleContext ctx, String comment) {
      if ((false == (ctx instanceof ANTLRv4Parser.GrammarSpecContext))) {
         writeCR();
      }
      write(comment);
      if ((true == ((ctx instanceof ANTLRv4Parser.GrammarSpecContext))) || (ctx instanceof ANTLRv4Parser.GrammarTypeContext)) {
         writeCR();
      }
   }

   /**
    * write a CR
    */
   private void writeCR() {
      writeSimple("\n");
      writeSimple(buildIndent(indent));
      newline = true;
   }

   private void writeSimple(String string) {
      try {
         writer.write(string);
      } catch (final IOException e) {
         throw new RuntimeException("Could not write to writer", e);
      }
   }
}
