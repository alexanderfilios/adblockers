clear all;
close all;
% Will not work on non-UNIX systems!
patharray = strsplit(mfilename('fullpath'), '/');
filepath_data = strcat(strjoin(patharray(1:end-2), '/'), '/figures/data');
filepath_plots = strcat(strjoin(patharray(1:end-2), '/'), '/figures/plots');

plot_titles = containers.Map({'first-means', 'third-means', 'first-stdev', 'third-stdev', 'density', 'misclassified', 'unrecognized'}, {' First means', 'Third means', 'First StdDev', 'Third StdDev', 'Density', 'Misclassified Reqs', 'Unrecognized Reqs'});
plot_labels = containers.Map({'first-means', 'third-means', 'first-stdev', 'third-stdev', 'density', 'misclassified', 'unrecognized'}, {' First means', 'Third means', 'First StdDev', 'Third StdDev', 'Density', 'Misclassified Reqs', 'Unrecognized Reqs'});
plot_colors = containers.Map({'data_Ghostery_Default', 'data_Ghostery_MaxProtection', 'data_Adblockplus_Default', 'data_Adblockplus_MaxProtection', 'data_NoAdblocker', 'data_NoAdblocker_DNT','data_Ghostery_Default_MUA', 'data_Ghostery_MaxProtection_MUA', 'data_Adblockplus_Default_MUA', 'data_Adblockplus_MaxProtection_MUA', 'data_NoAdblocker_MUA', 'data_NoAdblocker_DNT_MUA' }, {rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3), rand(1, 3)});

for file_data = transpose(dir(strcat([filepath_data '/*.csv'])))
    % Checking one metric, e.g. density.csv
    
    % Extracting data-file name and file
    filename_prefix = strsplit(file_data.name, '.');
    filename_prefix = filename_prefix(1);
    filename_data = strjoin([filepath_data, '/', filename_prefix, '.csv'], '');
    filename_plot = strjoin([filepath_plots, '/', filename_prefix, '.eps'], '');
    
    file = fopen(filename_data);
    
    % Read first line containing all headers
    row_cells = textscan(file, '%s', 'Delimiter', '\n');
    header_cells = textscan(row_cells{1,1}{1,1}, '%s', 'Delimiter', ',');
%     header_cells = strrep(header_cells{1}, '_', '\_');
    
    % Initializing the data matrix
    % Each row (starting from the 2nd) will contain a specific date
    % The first column will contain the date and the rest will contain the
    % value for an instance each
    data = zeros(length(row_cells{1}) - 1, length(header_cells{1}));
    for row_idx = 2:length(row_cells{1})
       row_string = row_cells{1}{row_idx};
       row_data_cell = textscan(row_string, '%s', 'Delimiter', ',');
       row_data = row_data_cell{1};
       data(row_idx, 1) = datenum(row_data{1});
       for col_idx = 2:length(row_data)
           if (~isempty(row_data{col_idx}))
            data(row_idx, col_idx) = str2double(row_data{col_idx});
           end
       end
    end
    
    % Plot the data for all instances and dates for the specific metric
    figure;
    hold on;
    data_size = size(data);
    title(plot_titles(filename_prefix{1}));
    for instance_idx = 2:data_size(2)
        plot(data(2:end, 1), data(2:end, instance_idx), 'color', plot_colors(header_cells{1}{instance_idx}));
    end
    dateaxis('x', 6);
    legends = strrep(header_cells{1}, '_', '\_');
    legend(legends{2:end});
    hold off;
    xlabel('Date');
    ylabel(plot_labels(filename_prefix{1}));
    
    % Save the file under the directory /figures/plots
    saveas(gcf, filename_plot, 'epsc');
end
    