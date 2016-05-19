clear all;
close all;

%% Constants

% Will not work on non-UNIX systems!
patharray = strsplit(mfilename('fullpath'), '/');
filepath_data = strcat(strjoin(patharray(1:end-2), '/'), '/figures/data');
filepath_plots = strcat(strjoin(patharray(1:end-2), '/'), '/figures/plots');

red = 'red';        % Ghostery color
blue = 'blue';      % Adblockplus color
green = [0 0.8 0];  % No adblocker color

default_width = 1;  % Line width for default settings
max_width = 4;      % Line width for max protection settings

desktop_style = '-';% Line style for desktop user agent
mobile_style = '--';% Line style for mobile user agent

FONT_SIZE = 18;

%% Time series

% Plots to be output
metrics = {
    'first-means'
    'third-means'
    'first-stdev'
    'third-stdev'
    'density'
    'misclassified'
    'unrecognized'
    'first-means-entities'
    'third-means-entities'
    'density-entities'
    'first-mean-top1'
    'first-mean-top10'
    'third-mean-top1'
    'third-mean-top10'
    'first-mean-top1-entities'
    'first-mean-top10-entities'
    'third-mean-top1-entities'
    'third-mean-top10-entities',
    'top500-first-means',
    'last500-first-means'};
titles = {
    'First means'
    'Third means'
    'First StdDev'
    'Third StdDev'
    'Density'
    'Misclassified Reqs'
    'Unrecognized Reqs'
    'First means with entities'
    'Third means with entities'
    'Density with entities'
    'FPD node degree'
    'Mean FPD node degree'
    'TPD node degree'
    'Mean TPD node degree'
    'FPD node degree with entities'
    'Mean FPD node degree with entities'
    'TPD node degree with entities'
    'Mean TPD node degree with entities',
    'Top 500 First means',
    'Last 500 Last means'};
labels = {
    'Mean FPD node degree'
    'Mean TPD node degree'
    'First StdDev'
    'Third StdDev'
    'Density'
    'Misclassified Reqs'
    'Unrecognized Reqs'
    'Mean FPD node degree'
    'Mean TPD node degree'
    'Density'
    'FPD node degree'
    'Mean FPD node degree'
    'TPD node degree'
    'Mean TPD node degree'
    'FPD node degree'
    'Mean FPD node degree'
    'TPD node degree'
    'Mean TPD node degree'
    'Mean FPD node degree'
    'Mean FPD node degree'};

% Instances plotted for each graph
instances = {
    'data_Ghostery_Default'
    'data_Ghostery_MaxProtection'
    'data_Adblockplus_Default'
    'data_Adblockplus_MaxProtection'
    'data_NoAdblocker'
    'data_NoAdblocker_DNT'
    'data_Ghostery_Default_MUA'
    'data_Ghostery_MaxProtection_MUA'
    'data_Adblockplus_MaxProtection_MUA'
    'data_Adblockplus_Default_MUA'
    'data_NoAdblocker_MUA'
    'data_NoAdblocker_DNT_MUA'};
colors = {red, red, blue, blue, green, green, red, red, blue, blue, green, green};
line_widths = {default_width, max_width, default_width, max_width, default_width, max_width, default_width, max_width, default_width, max_width, default_width, max_width};
line_styles = {desktop_style, desktop_style, desktop_style, desktop_style, desktop_style, desktop_style, mobile_style, mobile_style, mobile_style, mobile_style, mobile_style, mobile_style};

plot_titles = containers.Map(metrics, titles);
plot_labels = containers.Map(metrics, labels);
plot_colors = containers.Map(instances, colors);
plot_line_widths = containers.Map(instances, line_widths);
plot_line_styles = containers.Map(instances, line_styles);

for file_data = transpose(dir(strcat([filepath_data '/*.csv'])))
    % Checking one metric, e.g. density.csv
    
    % Extracting data-file name and file
    filename_prefix = strsplit(file_data.name, '.');
    filename_prefix = filename_prefix(1);
    if (isempty(find(ismember(metrics, filename_prefix), 1)))
        continue;
    end
    
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
%     title(plot_titles(filename_prefix{1}));
    for instance_idx = 2:data_size(2)
        plot(data(2:end, 1), data(2:end, instance_idx), ...
        plot_line_styles(header_cells{1}{instance_idx}), ...
        'color', plot_colors(header_cells{1}{instance_idx}), ...
        'LineWidth', plot_line_widths(header_cells{1}{instance_idx}));
    end
    datetick('x', 'keepticks');
    set(gca, 'FontSize', FONT_SIZE);
    
    legends = strrep(header_cells{1}, '_', '\_');
%      legend(legends{2:end});
    hold off;
    xlabel('Date', 'FontSize', FONT_SIZE);
    xlim([min(data(data(:, 1) ~= 0, 1)), max(data(data(:, 1) ~= 0, 1))]);
    ylim([0 inf]);
    ylabel(plot_labels(filename_prefix{1}), 'FontSize', FONT_SIZE);
    
    % Save the file under the directory /figures/plots
    saveas(gcf, filename_plot, 'epsc');
end


%% For one profile on one date

filename_prefix = 'scatterplot';

filename_data = strcat([filepath_data '/' filename_prefix '.csv']);
filename_plot = strcat([filepath_plots '/' filename_prefix '.eps']);
file = fopen(filename_data);

% Read first line containing all headers
row_cells = textscan(file, '%s', 'Delimiter', '\n');
header_cells = textscan(row_cells{1,1}{1,1}, '%s', 'Delimiter', ',');
values = csvread(strcat([filepath_data '/scatterplot.csv']), 1);


% Scatterplot and correlation
scatter(values(1:500, 2), values(1:500, 3), '.');
xlabel('Node degree', 'FontSize', FONT_SIZE);
ylabel('Relative rank', 'FontSize', FONT_SIZE);
set(gca, 'FontSize', FONT_SIZE);

corr(values(1:500, 2), values(1:500, 3))

% Save the file under the directory /figures/plots
saveas(gcf, strcat([filepath_plots '/scatterplot.eps']), 'epsc');

% CDFs
figure;
hold on;
[h, stats] = cdfplot(values(:, 3));
set(h, 'color', red);
[h, stats] = cdfplot(values(1:500, 3));
set(h, 'color', blue);
[h, stats] = cdfplot(values(501:1000, 3));
set(h, 'color', green);
hold off;
legend('Total 1000', 'Top-ranked 500', 'Uniformly-selected 500');
xlabel('Node degree', 'FontSize', FONT_SIZE);
ylabel('CDF', 'FontSize', FONT_SIZE);
set(gca, 'FontSize', FONT_SIZE);
xlim([0, 150]);
title('');

% Save the file under the directory /figures/plots
saveas(gcf, strcat([filepath_plots '/cdf-first-node-degree.eps']), 'epsc');



% close all;
    