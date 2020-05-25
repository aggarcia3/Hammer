// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer;

import java.util.EnumSet;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.Strings;
import com.beust.jcommander.UnixStyleUsageFormatter;
import com.beust.jcommander.WrappedParameter;

/**
 * Un formateador de sintaxis de parámetros de línea de comandos, idéntico al
 * {@link UnixStyleUsageFormatter} incluido con JCommander, excepto porque tiene
 * cadenas de texto en español.
 *
 * @author Alejandro González García
 */
final class FormateadorSintaxisParametros extends UnixStyleUsageFormatter {
	private final JCommander commander;

	public FormateadorSintaxisParametros(final JCommander commander) {
		super(commander);

		this.commander = commander;
	}

	@Override
	public void appendMainLine(StringBuilder out, boolean hasOptions, boolean hasCommands, int indentCount, String indent) {
        String programName = commander.getProgramDisplayName() != null
                ? commander.getProgramDisplayName() : "<clase principal>";
        StringBuilder mainLine = new StringBuilder();
        mainLine.append(indent).append("Sintaxis: ").append(programName);

        if (hasOptions) {
            mainLine.append(" [opciones]");
        }

        if (hasCommands) {
            mainLine.append(indent).append(" [comando] [opciones de comando]");
        }

        if (commander.getMainParameter() != null && commander.getMainParameterDescription() != null) {
            mainLine.append(" ").append(commander.getMainParameterDescription());
        }
        wrapDescription(out, indentCount, mainLine.toString());
        out.append("\n");
	}

	@Override
	public void appendAllParametersDetails(StringBuilder out, int indentCount, String indent,
			List<ParameterDescription> sortedParameters) {
        if (sortedParameters.size() > 0) {
            out.append(indent).append("  Opciones:\n");
        }

        // Calculate prefix indent
        int prefixIndent = 0;

        for (ParameterDescription pd : sortedParameters) {
            WrappedParameter parameter = pd.getParameter();
            String prefix = (parameter.required() ? "* " : "  ") + pd.getNames();

            if (prefix.length() > prefixIndent) {
                prefixIndent = prefix.length();
            }
        }

        // Append parameters
        for (ParameterDescription pd : sortedParameters) {
            WrappedParameter parameter = pd.getParameter();

            String prefix = (parameter.required() ? "* " : "  ") + pd.getNames();
            out.append(indent)
                    .append("  ")
                    .append(prefix)
                    .append(s(prefixIndent - prefix.length()))
                    .append(" ");
            final int initialLinePrefixLength = indent.length() + prefixIndent + 3;

            // Generate description
            String description = pd.getDescription();
            Object def = pd.getDefault();

            if (pd.isDynamicParameter()) {
                String syntax = "(sintaxis: " + parameter.names()[0] + "clave" + parameter.getAssignment() + "valor)";
                description += (description.length() == 0 ? "" : " ") + syntax;
            }

            if (def != null && !pd.isHelp()) {
                String displayedDef = Strings.isStringEmpty(def.toString()) ? "<cadena vacía>" : def.toString();
                String defaultText = "(valor predeterminado: " + (parameter.password() ? "********" : displayedDef) + ")";
                description += (description.length() == 0 ? "" : " ") + defaultText;
            }
            Class<?> type = pd.getParameterized().getType();

            if (type.isEnum()) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
				String valueList = EnumSet.allOf((Class<? extends Enum>) type).toString();

                // Prevent duplicate values list, since it is set as 'Options: [values]' if the description
                // of an enum field is empty in ParameterDescription#init(..)
                if (!description.contains("Opciones: " + valueList)) {
                    String possibleValues = "(valores: " + valueList + ")";
                    description += (description.length() == 0 ? "" : " ") + possibleValues;
                }
            }

            // Append description
            // The magic value 3 is the number of spaces between the name of the option and its description
            // in DefaultUsageFormatter#appendCommands(..)
            wrapDescription(out, indentCount + prefixIndent - 3, initialLinePrefixLength, description);
            out.append("\n");
        }
	}
}
